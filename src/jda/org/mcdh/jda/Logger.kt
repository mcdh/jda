package org.mcdh.jda

import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger

class Logger: IFernflowerLogger() {
 override fun writeMessage(message: String, level: Severity) {
  println("$level.prefix$message")
 }

 override fun writeMessage(message: String, level: Severity, exception: Throwable) {
  println("$level.prefix$message\nCaused by: ")
  exception.printStackTrace()
 }
}