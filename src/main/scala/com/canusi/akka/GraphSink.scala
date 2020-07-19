package com.canusi.akka

import scala.util.{Failure, Success, Try}

/**
 * Copyright Canusi
 *
 * @author Benjamin Hargrave
 */
trait GraphSink[Input] extends AbstractGraph {


  /**
   * Pulls in next value. This waits for upstream resources.
   * If upstream has no value, upstream will either close graph, or wait for next element
   * @param value
   */
  def pullIn(value: Input)

  def recoverPull(exception: Throwable, input: Input): Input = throw exception

  final def tryPullIn(input: Input): Unit = {
    Try( pullIn(input) ) match {
      case Success(value) => value
      case Failure(exception) =>
        tryRecoverPull( exception, input )
    }
  }

  final def tryRecoverPull(exception: Throwable, input: Input): Input = {
    Try( recoverPull( exception, input  )) match {
      case Success( value ) => value
      case Failure( exception ) =>
        throw new JobError.FailedToHandleError( exception )
    }
  }
}
