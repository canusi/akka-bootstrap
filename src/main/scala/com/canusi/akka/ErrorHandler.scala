package com.canusi.akka

/**
 * Copyright Canusi
 *
 * @author Benjamin Hargrave
 * @since 31.12.19
 */

object ErrorHandler {

  def convert2String( throwable: Throwable ): String  = {
    s"\nCause: ${throwable.getClass.getName}\nMessage: ${throwable.getMessage}\nStacktrace: ${throwable.getStackTrace.mkString("\n")}\n "
  }

}



