package com.canusi.akka

import java.util.UUID

import akka.actor.{ActorRef, PoisonPill}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}

/**
 * Copyright Canusi
 *
 * @author Benjamin Hargrave
 * @since 07.01.20
 */

import scala.concurrent.duration._
class JobScheduler(val akkaSystem: AkkaSystem) extends Logger {

  private val jobActor: ActorRef = akkaSystem.system.actorOf( JobActor(), s"job-${UUID.randomUUID().toString}")
  akkaSystem.system.registerOnTermination( Try{jobActor ! PoisonPill} )

  override protected val loggerName: String = "JobScheduler"

  import akkaSystem._

  def scheduleJobAsynchronously[T](job: Job[T]): Future[T] = {
    implicit val timeout: Timeout = 5.minutes // this is only the timeout for the job to be scheduled. It should be instant
    FutureUtil
      .futureWithTimeout[T](
        {jobActor ? job}.asInstanceOf[Future[T]]
        , job.timeoutMinutes.getOrElse(9999).minutes
      )
  }


  def scheduleJobSynchronously[T](request: Job[T] ): Unit = {
    awaitJob( request )
  }


  def scheduleParallelJobsSynchronously(parallelJobs: Job[_]*) = {
    var errorFound: Throwable = null
    parallelJobs.par.map {
      job =>
        Try( awaitJob( job ) ) match {
          case Failure(exception) => errorFound = exception
          case Success(value) => {}
        }
    }

    if (errorFound != null) throw errorFound
  }

  def awaitFuture[X]( future: Future[X], timeoutMinutes: Option[Int] = None  ): X = {
    awaitJob( Job( "Standalone future", s"Standalone future with timeout ${timeoutMinutes.getOrElse(0)}", timeoutMinutes, future ) )
  }

  import Job._
  def awaitJob[T](job: Job[T] ): T = {
    val timeoutDuration: Duration =
      job.timeoutMinutes match { case Some(value) => value minutes; case None => Duration.Inf }

    val future = scheduleJobAsynchronously( job )
    val futureResult = future.andThen {
      case Success(value) =>
        log.info(s"Finished Job Pipe ${job.name}. Jobs completed" )
        log.debug(s"Job Completed Description - ${job.description} - toString() = ${s"Job: ${job.toString}"}" )
        job.setResult(Left(value))
        job.stop()
      case Failure(exception) => {
        val desc = s"Job Graph: ${job.toString}" + "\n" +
          s"Job Description: ${job.description}\n" + "\n" +
          s"Job - toString() = ${s"Job: ${job.toString}"}" +
          s"Error: ${ErrorHandler.convert2String(exception)}"
        log.error(s"Failed Job Pipe ${job.name} ${desc}", exception )
        job.incrementErrors()
        job.setResult( Right(JobFailed(job.name, desc)))
        job.stop()
      }
    }


    Await.result(
      future, timeoutDuration
    )

    Await.result(
      futureResult, 1 minute
    )


    job.getResult match {
      case Left(value) => value
      case Right(value) => throw value
    }
  }


}


object JobScheduler{
  def apply(akkaSystem: AkkaSystem): JobScheduler = new JobScheduler(akkaSystem)
}

