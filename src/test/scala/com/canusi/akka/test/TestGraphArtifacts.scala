package com.canusi.akka.test

import com.canusi.akka.{GraphPipe, GraphSink, GraphSource}

import scala.collection.mutable

/**
 * Copyright Canusi
 *
 * @author Benjamin Hargrave
 */
object TestGraphArtifacts {

  object TestSource extends GraphSource[Int] {

    var counter: Int = 0

    override def hasMore: Boolean = counter < 5

    override def getNext: Int = {
      counter +=1
      counter
    }

    override def closeOn: Boolean = counter >= 5

    /**
     * Waits for elements if empty and not closed
     */
    override val sleepListenTimerMilliseconds: Option[Long] = None
  }

  object TestSourceGraph extends GraphSource[Int] {

    var counter: Int = 0

    override def hasMore: Boolean = counter < 5

    override def getNext: Int = {
      counter +=1
      counter
    }

    override def closeOn: Boolean = counter >= 5

    /**
     * Waits for elements if empty and not closed
     */
    override val sleepListenTimerMilliseconds: Option[Long] = None
  }

  object TestGraph extends GraphPipe[Int, Int] {

    private var last: Int = 0
    override def grabInput(input: Int): Unit = {
      last = input
      requestPush()
    }

    override def output(): Int = last

    override def afterOutput(): Unit = {}

    /**
     * Waits for elements if empty and not closed
     */
  }


  object TestGraphWithDelayedOutput extends GraphPipe[Int, Int] {

    val queue: mutable.Queue[Int] = mutable.Queue[Int]()
    var counter = 0
    override def onClose: Unit = {
      super.onClose
      requestPush()
    }

    override def grabInput(input: Int): Unit = {queue += input}

    override def output(): Int = {
      counter +=1
      queue.dequeue()
    }

    override def afterOutput(): Unit = {
      if( queue.nonEmpty )
        requestPush()
    }

  }




  object TestSink extends GraphSink[Int] {
    var lastValue: Int = -1
    override def pullIn(value: Int): Unit = {
      lastValue = value
      log.info( value + "" )
    }
  }

  object BadTestSink extends GraphSink[String] {
    override def pullIn(value: String): Unit = {
      println( value )
    }
  }
}

object TestSourceDelayed extends GraphSource[Int] {

  var counter: Int = 0

  var finishedReset = false

  override def hasMore: Boolean = {
    ( counter < 5 )
  }

  override def getNext: Int = {
    counter +=1
    if( counter == 5 && !finishedReset ){
      finishedReset = true
      counter = 0
    }
    log.info( counter + "")
    counter
  }

  override def closeOn: Boolean = counter >= 5 && finishedReset

  /**
   * Waits for elements if empty and not closed
   */
  override val sleepListenTimerMilliseconds: Option[Long] = None
}
