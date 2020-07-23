package com.canusi.akka.test

import com.canusi.akka.AkkaSystem
import com.typesafe.config.ConfigFactory

/**
 * Copyright Canusi
 *
 * @author Benjamin Hargrave
 * @since 2020-05-19 15:08
 *
 *
 *        This class is necessary because:
 *        This class supports the business purpose:
 *
 */
object TestActorSystem extends AkkaSystem( "TestActorSystem", ConfigFactory.load()) {


}
