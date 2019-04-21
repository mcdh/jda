package org.mcdh.jda

import org.jetbrains.java.decompiler.main.extern.IBytecodeProvider
import java.io.File

class BytecodeProvider: IBytecodeProvider {
 private val files: Map<String, File>

 public constructor(files: Map<String, File>) {
  this.files = files
 }

 override fun getBytecode(externalPath: String?, internalPath: String?): ByteArray {
  val path = externalPath!!.replace('\\', '/')
//   val file = files[externalPath]!!
  val file = files[path]
  if (file != null) {
   return file.readBytes()
  } else {
   throw AssertionError("$path not in ${files.keys}")
  }
 }
}