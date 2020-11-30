package com.canusi.akka

import java.util.UUID

import scala.concurrent.Future

/**
 * Copyright Canusi
 *
 * @author Benjamin Hargrave
 */
trait Job[T] extends StatusManager {
  import Job._
  val name: String
  val description: String
  val timeoutMinutes: Option[Int]
  val uuid: UUID = UUID.randomUUID()


  def run(): Future[T] = {
    if( !flagJobCalled ){
      flagJobCalled = true
      execute()
    } else {
      throw JobAlreadyExecutedException( this )
    }
  }

  private var result: Option[Either[T, JobFailed]] = None
  private var flagJobCalled: Boolean = false

  private[akka] def setResult( _result: Either[T, JobFailed]): Unit = result = Some(_result)

  protected def execute(): Future[T]

  def getResult: Either[T, JobFailed] = {
    result match {
      case None => throw ResultNotReadyException( this )
      case Some(value) => value
    }
  }

}



object Job{
  def apply[T](_name: String, _description: String, _timeoutMinutes: Option[Int], operation: => Future[T]): Job[T]
  = new Job[T]{
    override val name: String = _name
    override val description: String = _description
    override val timeoutMinutes: Option[Int] = _timeoutMinutes
    override def execute(): Future[T] = operation
  }

  case class JobAlreadyExecutedException[T]( job: Job[T] ) extends RuntimeException( s"Job already executed - parameters: ${job.toString}")
  case class ResultNotReadyException[T]( job: Job[T] ) extends RuntimeException( s"Job result is not ready - parameters: ${job.toString}")
  case class JobFailed(name: String, description: String) extends RuntimeException(s"Failed Job Pipe ${name}: ${description}.")

}
