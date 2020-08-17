package com.canusi.akka

import scala.util.{Failure, Success, Try}

/**
 * Copyright Canusi
 *
 * @author Benjamin Hargrave
 */
trait AbstractGraphSource[Output] extends AbstractGraph {


  /**
   * Returns next output to be pushed
   * @return
   */
  def getNext: Output

  /**
   * Iterator use-case. If true, [[getNext()]] is called. If false, checks [[isClosed()]].
   * @return
   */
  def hasMore: Boolean


  def recoverPush(exception: Throwable): Output =  throw exception

  protected def checkForClose(): Unit

  final def tryGetNext: Output = {
    val next = Try( getNext ) match {
      case Success(value) => value
      case Failure(exception) =>
        tryRecoverPush( exception )
    }
    checkForClose()
    next
  }

  final def tryRecoverPush(exception: Throwable): Output = {
    Try( recoverPush( exception )) match {
      case Success( value ) => value
      case Failure( exception ) =>
        throw new JobError.FailedToHandleError( exception )
    }
  }

}
