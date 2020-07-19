package com.canusi.akka

import akka.stream.stage._
import akka.stream.{Attributes, Outlet, SourceShape}


class AkkaSource[Output](job: GraphSource[Output]) extends GraphStage[SourceShape[Output]] {

  private val out: Outlet[Output] = Outlet[Output](s"${job.getClass.getName}.out")
  override val shape: SourceShape[Output] = SourceShape(out)

  override def createLogic(attr: Attributes): GraphStageLogic =
    new GraphStageLogicWithLogging(shape) with OutHandler {

      override def preStart(): Unit = {
        super.preStart()
        job.setLogger( this.log )
        job.tryPreTask()
        log.info( "Prestart finished source")
      }

      override def onPull(): Unit = {
        log.info( "Pushing from source")
        pushOut()
      }


      def pushOut(): Unit = {
        if( job.hasMore ){
          push( out, job.tryGetNext )
        } else if( job.closeOn || job.isClosed() ){
          job.close()
          completeStage()
        } else {
          sleep()
          pushOut()
        }
      }

      val sleepTimer: Long = job.sleepListenTimerMilliseconds match { case None => 1000L; case Some(value) => value }

      def sleep(): Unit ={
        Thread.sleep( sleepTimer )
      }


      override def postStop(): Unit = {
        job.tryPostTask()
        super.postStop()
      }

      setHandler( out , this )
    }
}

object AkkaSource {
  def apply[Output](job: GraphSource[Output]): AkkaSource[Output] = new AkkaSource(job)
}

