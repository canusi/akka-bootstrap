package com.canusi.akka

import scala.concurrent.Future

/**
 * Copyright Canusi
 *
 * @author Benjamin Hargrave
 */
trait Job[T]{
  val name: String
  val description: String
  val timeoutMinutes: Option[Int]
  def execute(): Future[T]
}

object Job{
  def apply[T](_name: String, _description: String, _timeoutMinutes: Option[Int], operation: => Future[T]): Job[T]
  = new Job[T]{
    override val name: String = _name
    override val description: String = _description
    override val timeoutMinutes: Option[Int] = _timeoutMinutes
    override def execute(): Future[T] = operation
  }
}
