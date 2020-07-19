package com.canusi.akka

import akka.event.{Logging, LoggingAdapter}

import scala.util.{Failure, Success, Try}

/**
 * Copyright Canusi
 *
 * @author Benjamin Hargrave
 */
trait AbstractGraph {

  private var logger: LoggingAdapter = null

  private[akka] def setLogger( logger: LoggingAdapter ) = {
    this.logger = logger
  }

  lazy val log: LoggingAdapter = logger

  def preTask(): Unit = {}

  def postTask(): Unit = {}

  final def tryPreTask(): Unit = {
    Try( preTask() ) match {
      case Success(_) =>
      case Failure( exception ) => throw new JobError.FailedOnStart( exception )
    }
  }

  final def tryPostTask(): Unit = {
    Try( postTask() ) match {
      case Success(_) =>
      case Failure( exception ) => throw new JobError.FailedOnShutDown( exception )
    }
  }

}

object JobError {

  class FailedOnStart(exception: Throwable)
    extends RuntimeExceptionWrapper(s"Critical. Failed to start task: ", exception )
  class FailedOnShutDown( exception: Throwable)
    extends RuntimeExceptionWrapper(s"Critical. Failed to handle shutdown: ", exception )
  class FailedToHandleError(exception: Throwable)
    extends RuntimeExceptionWrapper(s"Critical. Failed to handle exception: ", exception )

}

class RuntimeExceptionWrapper( context: String, exception: Throwable ) extends RuntimeException{
  override def getMessage: String = {
    context + " -> " + exception.getMessage
  }
  override def getCause: Throwable = exception.getCause
  override def getStackTrace: Array[StackTraceElement] = exception.getStackTrace
  override def printStackTrace(): Unit = exception.printStackTrace()

}
