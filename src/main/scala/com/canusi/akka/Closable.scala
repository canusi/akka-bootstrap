package com.canusi.akka

/**
 * Copyright Canusi
 *
 * @author Benjamin Hargrave
 */
trait Closable {

  private var closeFlag = false

  def onClose(): Unit = {}

  def close(): Unit = {
    onClose()
    closeFlag = true
  }

  final def isClosed(): Boolean = closeFlag

}
