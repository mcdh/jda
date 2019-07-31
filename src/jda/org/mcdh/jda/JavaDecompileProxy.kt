package org.mcdh.jda

import org.jetbrains.java.decompiler.main.decompiler.BaseDecompiler
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger
import java.io.File

class JavaDecompileProxy @JvmOverloads constructor(
 private val options: Map<String, String>,
 private val classpath: List<String> = listOf(),
 private val saver: ResultSaver = ResultSaver(),
 private val logger: IFernflowerLogger = Logger()
) {
 init {
  println("Current options: $options")
 }

 fun decompile(path: String): String {
  val target = File(path)
  val files = mutableMapOf(Pair(sanitize(path), target))
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
     && it.nameWithoutExtension.startsWith(target.nameWithoutExtension + '$', false)
     && it.extension.equals("class", true)
   } as MutableList<File>
   //Assemble files map
   children.forEach {
    files[it.absolutePath] = it
   }
   //Construct options map
   val options = mutableMapOf<String, Any>()
   options.putAll(this.options)
   //TODO add switch option for line mappings
   if (true) {
    options["bsm"] = "1"
   } else {
    options["__dump_original_lines__"] = "1"
   }
   //Decompilation
   val provider = BytecodeProvider(files)
   val decompiler = BaseDecompiler(provider, saver, options, logger)
   classpath.forEach {
    val file = File(it)
    if (file.exists() && file.extension.equals("class") || file.extension.equals("jar")) {
     decompiler.addLibrary(file)
    }
   }
   files.keys.forEach {
    decompiler.addSource(File(it))
   }
   decompiler.decompileContext()
  } catch(t: Throwable) {
   throw t
  }
  return saver.getResult()
 }
}