package com.canusi.akka

import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}

/**
 * Copyright Canusi
 *
 * @author Benjamin Hargrave
 * @since 07.01.20
 */




class JobScheduler(val akkaSystem: AkkaSystem)
  extends Logger {

  override protected val loggerName: String = "JobScheduler"

  import akkaSystem._

  import scala.concurrent.duration._

  def scheduleJobAsynchronously[T](job: Job[T]): Future[T] = {

    val future: Future[T] = job.execute()
    job.timeoutMinutes match {
      case Some(value) => FutureUtil.futureWithTimeout( future, value minutes);
      case None => future
    }
  }


  def scheduleJobSynchronously[T](request: Job[T] ): Unit = {
    awaitFuture( request )
  }


  def scheduleParallelJobsSynchronously(parallelJobs: Job[_]*) = {
    var errorFound: Throwable = null
    parallelJobs.par.map {
      jobFuture =>
        Try( awaitFuture( jobFuture ) ) match {
          case Failure(exception) => errorFound = exception
          case Success(value) => {}
        }
    }

    if (errorFound != null) throw errorFound
  }


  def awaitFuture[T]( job: Job[T] ): Unit = {
    var result: Option[JobFailed] = null
    val timeoutDuration: Duration =
      job.timeoutMinutes match { case Some(value) => value minutes; case None => Duration.Inf }

    val future = scheduleJobAsynchronously( job )

    val futureResult = future.andThen {
      case Success(value) =>
        log.info(s"Finished Job Pipe ${job.name}. Jobs completed" )
        log.debug(s"Job Completed Description - ${job.description} - toString() = ${s"Job: ${job.toString}"}" )
        result = None
      case Failure(exception) => {
        val desc = s"Job Graph: ${job.toString}" + "\n" +
          s"Job Description: ${job.description}\n" + "\n" +
          s"Job - toString() = ${s"Job: ${job.toString}"}" +
          s"Error: ${ErrorHandler.convert2String(exception)}"
        log.error(s"Failed Job Pipe ${job.name} ${desc}", exception )
        result = Some(new JobFailed(job.name, desc))
      }
    }


    Await.result(
      future, timeoutDuration
    )

    Await.result(
      futureResult, 1 minute
    )

    result match {
      case None => {}
      case Some(value) => throw value
    }
  }


  private class JobFailed(name: String, description: String) extends Exception(s"Failed Job Pipe ${name}: ${description}.")


}


object JobScheduler{
  def apply(akkaSystem: AkkaSystem): JobScheduler = new JobScheduler(akkaSystem)
}

