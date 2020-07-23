package com.canusi.akka.test

import akka.stream.ActorMaterializer
import akka.testkit.{ImplicitSender, TestKit}
import com.canusi.akka.GraphBuilder
import com.canusi.akka.test.TestGraphArtifacts.{TestGraph, TestGraphWithDelayedOutput, TestSink, TestSource, TestSourceGraph}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
 * Copyright Canusi
 *
 * @author Benjamin Hargrave
 */
class test_AkkaBuilder extends TestKit(TestActorSystem.system)
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  implicit val materalizer: ActorMaterializer = ActorMaterializer()

    "Builder" should {

      "run with source that pauses output" in {
        val builder = GraphBuilder.start( TestSourceDelayed )
        val runningGraph = builder.close( TestSink )
        TestSourceDelayed.counter shouldEqual 0

        Await.result( runningGraph.run( materalizer ), Duration.Inf )
        TestSourceDelayed.counter shouldEqual 5
      }
      "run basic source/sink" in {
        val builder = GraphBuilder.start( TestSource ).close( TestSink )
        val runningGraph = builder.run( materalizer )
        TestSource.counter shouldEqual 0
        Await.result( runningGraph, Duration.Inf )
        TestSource.counter shouldEqual 5
      }
      "run basic graph" in {
        TestSourceGraph.counter = 0
        val builder =
          GraphBuilder
          .start( TestSourceGraph )
          .add( TestGraph )
          .close( TestSink )
        val runningGraph = builder.run( materalizer )
        TestSourceGraph.counter shouldEqual 0
        Await.result( runningGraph, Duration.Inf )
        TestSourceGraph.counter shouldEqual 5
      }

      "run graph with delayed output" in {
        TestSourceGraph.counter = 0
        val builder =
          GraphBuilder
            .start( TestSourceGraph )
            .add( TestGraphWithDelayedOutput )
            .close( TestSink )
        TestSourceGraph.counter shouldEqual 0
        val runningGraph = builder.run( materalizer )
        Await.result( runningGraph, Duration.Inf )
        TestGraphWithDelayedOutput.counter shouldEqual 5
        TestSink.lastValue shouldEqual 5
      }
    }
}


