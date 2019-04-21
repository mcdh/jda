package org.mcdh.jda

import java.io.File
import java.nio.file.Files
import kotlin.streams.toList
import kotlin.system.exitProcess

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
 val rootAbsolutePath = sanitize(paths[0])
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
       val path = sanitize(it.toString())
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
 val decompiler = JavaDecompileProxy(flags)
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
   val absolutePath = sanitize(it.absolutePath)
   Files.write(
    File(absolutePath.replace(rootAbsolutePath, "")).toPath(),
    decompiler.decompile(absolutePath as java.lang.String).bytes
   )
  }
}

fun sanitize(input: String): String {
 return input.replace('\\', '/')
}