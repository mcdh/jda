package org.mcdh.jda

import org.jetbrains.java.decompiler.main.decompiler.BaseDecompiler
import java.io.File

import kotlin.text.*

class JavaDecompileProxy {
 private val saver = ResultSaver()

 val logger = Logger()

 fun decompile(path: java.lang.String): java.lang.String {
  val target = File(path as String)
  val files = mutableMapOf<String, File>(Pair(path, target))
  val mask = target.nameWithoutExtension + '$'
  try {
   val parent = target.parentFile
   var children = mutableListOf<File>(parent)
   var childAdded = true
   //Get all files in directory
   while(childAdded) {
    childAdded = false
    val toAdd = mutableListOf<File>()
    val toRemove = mutableListOf<File>()
    for(file in children) {
     if (file.isDirectory) {
      toRemove.add(file)
      val files = file.listFiles()
      if (files.isNotEmpty()) {
       childAdded = true
       toAdd.addAll(files)
      }
     }
    }
    children.removeAll(toRemove)
    children.addAll(toAdd)
   }
   children = children.filter {
    !it.isDirectory
//     && it.nameWithoutExtension.startsWith(mask, 2, false)
     && it.nameWithoutExtension.startsWith(mask, false)
     && it.extension.equals(".class", true)
   } as MutableList<File>
   //Assemble files map
   children.forEach {
    files[it.absolutePath] = it
   }
   //Construct options map
   val options = mutableMapOf<String, Any>()
   //TODO add switch option for line mappings
   if (true) {
    options["bsm"] = "1"
   } else {
    options["__dump_original_lines__"] = "1"
   }
   //Decompilation
   val provider = BytecodeProvider(files)
   //moved to global declaration
//   val saver = ResultSaver()
   val decompiler = BaseDecompiler(provider, saver, options, logger)
   files.keys.forEach {
    decompiler.addSource(File(it))
   }
   decompiler.decompileContext()
  } catch(t: Throwable) {
   throw t
  }
  return saver.getResult() as java.lang.String
 }
}