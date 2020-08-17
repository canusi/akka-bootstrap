package com.canusi.akka

/**
 * Copyright Canusi
 *
 * @author Benjamin Hargrave
 */
trait GraphSource[Output] extends AbstractGraphSource[Output] with Closable   {


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


  override def checkForClose(): Unit = {
    if( closeOn ){
      close()
    }
  }

}
