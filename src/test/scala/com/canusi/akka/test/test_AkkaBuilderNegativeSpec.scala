package com.canusi.akka.test

import akka.testkit.{ImplicitSender, TestKit}
import com.canusi.akka.test.TestGraphArtifacts.TestSource
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

/**
 * Copyright Canusi
 *
 * @author Benjamin Hargrave
 */
class test_AkkaBuilderNegativeSpec extends TestKit(TestActorSystem.system)
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  "Builder" should {
  }
}


