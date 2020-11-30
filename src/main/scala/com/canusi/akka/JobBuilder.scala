package com.canusi.akka

/**
 * Copyright Canusi
 *
 * @author Benjamin Hargrave
 */
trait JobBuilder[X] {
  def build(): Job[X]
}
