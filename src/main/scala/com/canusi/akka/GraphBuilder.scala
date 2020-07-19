package com.canusi.akka

import akka.Done
import akka.event.Logging
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{Attributes, Materializer}
import akka.util.Timeout

import scala.concurrent.Future

/**
 * Copyright Canusi
 *
 * @author Benjamin Hargrave
 */
object GraphBuilder {

  def start[Output]( sourceJob: GraphSource[Output]): GraphBuildingStage[Output] = {
    new GraphBuildingStage[Output](
      Source.fromGraph( AkkaSource( sourceJob ) )
        .log("before-map")
        .withAttributes(
          Attributes
          .logLevels(onElement = Logging.WarningLevel
            , onFinish = Logging.InfoLevel, onFailure = Logging.DebugLevel))

    )
  }
  class IllegalGraphConnection(str: String) extends RuntimeException(str)


  class GraphBuildingStage[Input]( source: Source[Input, _] ) {
    def add[Output](graphPipe: GraphPipe[Input, Output]): GraphBuildingStage[Output] = {
      new GraphBuildingStage( source.via( AkkaGraph( graphPipe)) )
    }


    def close(sink: GraphSink[Input]): ClosedGraph[Input] =
      new ClosedGraph( source, sink )
  }

  class ClosedGraph[Input]( source: Source[Input, _], sink: GraphSink[Input] ) {
    import scala.concurrent.duration._
    def run(materializer: Materializer, timeout: Timeout = Timeout.apply( 365 * 291 days ) ): Future[Done] =
      source.runWith( Sink.fromGraph( AkkaSink( sink ) ) )( materializer )
  }
}




