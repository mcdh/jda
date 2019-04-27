package org.mcdh.jda

import com.intellij.util.containers.stream
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences
import java.io.File
import java.nio.file.Files
import kotlin.streams.toList
import kotlin.system.exitProcess

//TODO
fun main(args: Array<String>) {
 val flags = mutableMapOf<String, String>()
 val paths = mutableListOf<String>()
 for (arg in args) {
  //Parse fernflower option flags
  if (arg.contains("=") && arg.contains("-")) {
   val options = arg.replace("-", "").split("=")
   flags[options[0]] = options[1]
  } else {
   //Parse inputs
   paths.add(arg)
  }
 }
 if (paths.size != 2) {
  println("You must specify an input and output directory.")
  exitProcess(255)
 }
 //Process input files
 val toDecompile = mutableListOf<File>()
 val rootAbsolutePath = sanitizePath(paths[0])
 val root = File(rootAbsolutePath)
 if (root.isDirectory) {
  var additionsMade = true
  while (additionsMade) {
   additionsMade = false
   val toAdd = mutableListOf<File>()
   val toRemove = mutableListOf<File>()
   for (parent in toDecompile) {
    if (parent.isDirectory) {
     additionsMade = true
     val adding: List<File> = Files
      .list(parent.toPath())
      .filter {
       val path = sanitizePath(it.toString())
       path.endsWith(".class")
        && !path.substring(path.lastIndexOf("/")).contains("\$")
      }
      .map { it.toFile() }
      .toList()
     toRemove.add(parent)
     toAdd.addAll(adding)
    }
   }
   toDecompile.removeAll(toRemove)
   toDecompile.addAll(toAdd)
  }
 } else {
  toDecompile.add(root)
 }
 //Decompile
 val decompiler = JdaDecompilerContext(flags)
 val outputPath = paths[1]
 val output = File(outputPath)
 if (output.exists()) {
  output.delete()
 }
 output.mkdirs()
 toDecompile
//  .parallelStream()
  .stream()
  .forEach {
   val absolutePath = sanitizePath(it.absolutePath)
   val toOutputTo = absolutePath.replace(rootAbsolutePath, "")
   println("OUTPUTTING TO $toOutputTo")
   println(decompiler.decompile(absolutePath))
   //TODO Fix output writing
//   Files.write(
//    File(toOutputTo).toPath(),
//    decompiler.decompile(absolutePath).toByteArray()
//   )
  }
}

fun sanitizePath(input: String): String {
 return input.replace('\\', '/')
}

fun fernflowerDefaultOptions(): Map<String, Any> {
//  val optionsMap: Map<String, Any> = mutableMapOf()
 val optionsMap = mutableMapOf<String, Any>()
 IFernflowerPreferences.DEFAULTS.forEach { (k: String, v: Any) ->
  optionsMap[k] = v
 }
 return optionsMap
}

fun findSubclasses(clazz: File): Map<String, File> {
 val mask = clazz.nameWithoutExtension + '$'
 val innerClasses = mutableMapOf(Pair(sanitizePath(clazz.absolutePath), clazz))
 clazz
  .listFiles()
  .stream()
  .filter {
   !it.isDirectory
    && it.nameWithoutExtension.startsWith(mask, false)
    && it.extension.equals("class", true)
  }
  .forEach {
   innerClasses[sanitizePath(it.absolutePath.toString())] = it
  }
 return innerClasses
}