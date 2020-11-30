package com.canusi.akka

import akka.actor.{Actor, ActorLogging, Props, SupervisorStrategy}

import scala.concurrent.Future

/**
 * Copyright Canusi
 *
 * @author Benjamin Hargrave
 */

class JobActor extends Actor with ActorLogging {
  import context.dispatcher

  override def receive = {
    case job: Job[_] =>
      val origin = sender()
      origin ! job.run()
        .map{ value =>
          log.debug(s"Job Completed Description - ${job.description} - toString() = ${s"Job: ${job.toString}"}")
          job.stop()
        }.recoverWith {
          case exception: Throwable => Future{
            val desc = s"Job Graph: ${job.toString}" + "\n" +
              s"Job Description: ${job.description}\n" + "\n" +
              s"Job - toString() = ${s"Job: ${job.toString}"}" +
              s"Error: ${ErrorHandler.convert2String(exception)}"
            log.error(exception, s"Failed Job Pipe ${job.name} ${desc}")
            job.incrementErrors()
            job.stop()
          }
        }
    case SupervisorStrategy.Stop => context.stop(self)
  }


}
object JobActor{
  def apply(): Props = Props[JobActor]
}
