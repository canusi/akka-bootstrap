package com.canusi.akka.test

import akka.Done
import akka.stream.ActorMaterializer
import akka.testkit.{ImplicitSender, TestKit}
import com.canusi.akka.{Job, JobScheduler, test}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.Future

class test_Schedulerextends extends TestKit(TestActorSystem.system)
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  import TestActorSystem._

  val jobScheduler = JobScheduler(TestActorSystem)

  "Scheduler" should {
    "start job asyncronously" in {
      jobScheduler.scheduleJobAsynchronously(TestJobs.simple)
    }
    "complete parallel jobs irrespective of individual failure" in {
      jobScheduler.scheduleParallelJobsSynchronously( TestJobs.simple, TestJobs.wait2Seconds, TestJobs.wait3Seconds, TestJobs.wait3Seconds)
    }
    "start job and wait" in {
      jobScheduler.scheduleJobSynchronously( TestJobs.wait2Seconds)
    }

  }


}


object TestJobs {

  val simple: Job[Done] = Job[Done]("TestJob", "Returns a done future", None, Future(Done)(TestActorSystem.executorContext))

  val wait2Seconds: Job[Done] = Job[Done]("JobWith2SecondWait"
    , "Returns a done future after sleeping 2 seconds"
    , None
    , Future({
      Thread.sleep( 2000L)
      Done
    })(TestActorSystem.executorContext))

  val wait3Seconds: Job[Done] = Job[Done]("JobWith3SecondWait"
    , "Returns a done future"
    , None
    , Future({
      Thread.sleep( 3000L)
      Done})(TestActorSystem.executorContext))

}
