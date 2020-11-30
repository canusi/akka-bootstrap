package com.canusi.akka

import java.util.TimerTask

import akka.stream.stage._
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}

/**
 * Copyright Canusi
 *
 * @author Benjamin Hargrave
 */
object AkkaGraph {

  def apply[Input, Output](job: GraphPipe[Input, Output]): AkkaGraph[Input, Output] = new AkkaGraph(job)

}

class AkkaGraph[Input, Output](job: GraphPipe[Input, Output])
  extends GraphStage[FlowShape[Input, Output]] {

  private val in: Inlet[Input] = Inlet[Input](s"${job.getClass.getName}.in")
  private val out: Outlet[Output] = Outlet[Output](s"${job.getClass.getName}.out")
  val shape = FlowShape.of(in, out)

  override def createLogic(attr: Attributes): GraphStageLogic =
    new GraphStageLogicWithLogging(shape) with InHandler with OutHandler {

      override def preStart(): Unit = {
        super.preStart()
        job.setLogger(this.log)
        job.tryPreTask()
        log.info("Prestart finished graph")
      }

      override def onPull(): Unit = {
        pushOut()
      }

      var waitingOnPull = false
      var stageCompleted = false
      var taskScheduled = false

      def pushOut(): Unit = {
        job.preparePush()
        if (job.hasMore) {
          push(out, job.tryGetNext)
        } else {
          // todo this needs a feature extension within the graph pipe
          if( !job.isClosed() ){
            waitingOnPull = true
            pull(in)
          } else if(job.isClosed() && job.getWaitForOutput() ){
            log.debug( s"Sleeping ${this}. Waiting for job to process.")
            Thread.sleep(1000L)
            pushOut()
          } else if (job.isClosed() && !job.getWaitForOutput() ){
            log.info("Closing Graph")
            completeStage()
          }
        }
      }

      private class StartJobTask extends TimerTask {
        override def run(): Unit = {
          pushOut()
        }
      }

      override def onUpstreamFailure(ex: Throwable): Unit = {
        job.close()
        pushOut()
        failStage(ex)
      }

      /**
       * Pull elements until the job is ready to emit an output
       * When the input is closed, this will continue pushing, until the job has no more elements.
       */
      override def onPush(): Unit = {
        job.tryPullIn(grab(in))
        if( waitingOnPull ){
          waitingOnPull = false
          pushOut()
        }
      }


      setHandler(in, this)
      setHandler(out, this)

      /**
       * Mark the job as closed. This will prevent the pull loop continuing
       */
      override def onUpstreamFinish(): Unit = {
        job.close()
        pushOut()
      }

      override def postStop(): Unit = {
        job.tryPostTask()
        super.postStop()
      }

    }
}

