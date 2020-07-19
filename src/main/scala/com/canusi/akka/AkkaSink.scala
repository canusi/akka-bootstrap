package com.canusi.akka

import akka.Done
import akka.stream.stage._
import akka.stream.{AbruptStageTerminationException, Attributes, Inlet, SinkShape}

import scala.concurrent.{Future, Promise}


class AkkaSink[Input](job: GraphSink[Input])
  extends GraphStageWithMaterializedValue[SinkShape[Input], Future[Done]] {

  private val in: Inlet[Input] = Inlet[Input](s"${job.getClass.getName}.in")
  override val shape: SinkShape[Input] = SinkShape(in)


  override def createLogicAndMaterializedValue(inheritedAttributes: Attributes): (GraphStageLogic, Future[Done]) = {
    val finishPromise = Promise[Done]()

    (new GraphStageLogicWithLogging(shape) with InHandler {

      override def preStart(): Unit = {
        super.preStart()
        job.setLogger(this.log)
        job.tryPreTask()
        tryPull(in)
      }

      override def onUpstreamFinish(): Unit = {
        finishPromise.success(Done)
        log.info( "Closing sink")
        completeStage()
      }

      override def onUpstreamFailure(ex: Throwable): Unit = {
        finishPromise.failure(ex)
        failStage(ex)
      }

      def onPush(): Unit = {
        job.tryPullIn(grab(in))
        tryPull(in)
      }



      override def postStop(): Unit = {

        if (!finishPromise.isCompleted) {
          finishPromise.failure(new AbruptStageTerminationException(this))
        }
        job.tryPostTask()
        super.postStop()
      }

      setHandler(in, this)

    }, finishPromise.future)
  }

}

object AkkaSink{

  def apply[Input](job: GraphSink[Input]): AkkaSink[Input] = new AkkaSink(job)


}

