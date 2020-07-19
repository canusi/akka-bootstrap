package com.canusi.akka

import scala.util.{Failure, Success, Try}

/**
 * Copyright Canusi
 *
 * @author Benjamin Hargrave
 */
trait GraphSource[Output] extends AbstractGraph {


  /**
   * Timer to sleep if [[hasMore]] = false and [[isClosed]] = false
   * Defaults to 1000L if set to None. If no sleep is required, implement {{{ closeOn = { hasMore = false } }}}
   */
  val sleepListenTimerMilliseconds: Option[Long]

  /**
   * Allows a graph to close itself.
   * Called after [[getNext()]]. If true, closes graph.
   * @return
   */
  def closeOn: Boolean

  /**
   * Iterator use-case. If true, [[getNext()]] is called. If false, checks [[isClosed()]].
   * If [[isClosed()]] = true, begins shutting down the graph. If false, waits using [[sleepListenTimerMilliseconds]]
   * @return
   */
  def hasMore: Boolean


  /**
   * Returns next output to be pushed
   * @return
   */
  def getNext: Output

  private var closeFlag  = false
  def close(): Unit = { closeFlag = true }
  final def isClosed(): Boolean = closeFlag

  def recoverPush(exception: Throwable): Output =  throw exception

  final def tryGetNext: Output = {
    val next = Try( getNext ) match {
      case Success(value) => value
      case Failure(exception) =>
        tryRecoverPush( exception )
    }
    if( closeOn ){ close() }
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
