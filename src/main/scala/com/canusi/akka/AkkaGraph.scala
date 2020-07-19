package com.canusi.akka

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

      def pushOut(): Unit = {
        if (job.hasMore) {
          push(out, job.tryGetNext)
        } else {
          if (job.isClosed()) {
            log.info("Closing Graph")
            completeStage()
          } else {
            waitingOnPull = true
            pull(in)
          }
        }
      }

      override def onUpstreamFailure(ex: Throwable): Unit = {
        ???
        job.close()
        pushOut()
        failStage(ex)
      }

      /**
       * Pull elements until the job is ready to emit an output
       * When the input is closed, this will continue pushing, until the job has no more elements.
       */
      override def onPush(): Unit = {
        job.pullIn(grab(in))
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

