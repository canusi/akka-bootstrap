package com.canusi.akka

import scala.annotation.tailrec
import scala.collection.mutable

/**
 * Copyright Canusi
 *
 * @author Benjamin Hargrave
 */
trait GraphPipe[Input, Output] extends GraphSource[Output]
  with GraphSink[Input] {

  private lazy val internalQueue = mutable.Queue[Output]()

  private var pushRequested: Boolean = false

  final override def pullIn(input: Input): Unit = {
    grabInput(input)
    handlePush(input)
  }

  @tailrec
  private def handlePush(input: Input): Unit ={
    if(pushRequested){
      internalQueue += output()
      pushRequested = false
      afterOutput()
      handlePush(input)
    }
  }

  final protected def requestPush(): Unit = pushRequested = true


  /**
   * Input handler from upstream elements. Will wait for element to arrive indefinitely.
   * Will not push output, until `requestPush()` is called
   * @param input
   */
  def grabInput(input: Input): Unit

  /**
   * Called after call to `requestPush()`. Pushes next output
   * @return
   */
  def output(): Output

  /**
   * Called after output(). A call to `requestPush()` will push another element.
   * Output loop can be setup using this, with one `grabInput` call, generating many `output` results
   * @return
   */
  def afterOutput(): Unit

  final override def closeOn: Boolean = false

  def onClose(): Unit = {}

  final override val sleepListenTimerMilliseconds: Option[Long] = None

  final override def hasMore: Boolean = internalQueue.nonEmpty

  final override def getNext: Output = internalQueue.dequeue()

  final override def close(): Unit = {
    super.close()
    onClose()
    closeOutput()
  }


  @tailrec
  private def closeOutput(): Unit  = {
    if( pushRequested ){
      log.info( "Trying extended close")
      internalQueue += output
      pushRequested = false
      afterOutput()
      closeOutput()
    }
  }

}
