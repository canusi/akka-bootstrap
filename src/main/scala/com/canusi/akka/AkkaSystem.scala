package com.canusi.akka

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.ExecutionContextExecutor

/**
 * Copyright Canusi
 *
 * @author Benjamin Hargrave
 */
class AkkaSystem( actorSystemName: String, config: Config) {
  // setup implicits for other actors
  lazy implicit val system: ActorSystem = ActorSystem( actorSystemName, config )
  lazy implicit val materializer: ActorMaterializer = ActorMaterializer()
  lazy implicit val executorContext: ExecutionContextExecutor = system.dispatcher


  system.registerOnTermination( materializer.shutdown() )

  def shutDown(): Unit ={
    system.terminate()
  }
}

object AkkaSystem {

  def apply(actorSystemName: String = "DefaultActorSystem", config: Config = ConfigFactory.load()): AkkaSystem
  = new AkkaSystem(actorSystemName, config)

}
