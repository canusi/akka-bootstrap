package com.canusi.akka

import com.typesafe.scalalogging
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

/**
 * Copyright Canusi
 *
 * @author Benjamin Hargrave
 */
trait Logger {

  protected val loggerName: String

  lazy val log: scalalogging.Logger = Logger(LoggerFactory.getLogger(loggerName))

}
