package org.mcdh.jda

import java.io.File
import java.nio.file.Files
import java.nio.file.StandardOpenOption
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
 val toDecompile = mutableSetOf<String>()
 val rootAbsolutePath = File(sanitize(paths[0])).absolutePath
 var additionsMade = true
 toDecompile.add(rootAbsolutePath)
 while (additionsMade) {
  additionsMade = false
  val toAdd = mutableListOf<String>()
  val toRemove = mutableListOf<String>()
  for (path in toDecompile) {
   val sp = sanitize(path)
   val f = File(sp)
   if (f.isDirectory) {
    additionsMade = true
    f.list()!!.forEach {
//     toAdd.add("$rootAbsolutePath/$it")
     toAdd.add("$sp/$it")
    }
    toRemove.add(sp)
   } else {
    val fileName = sp.substringAfterLast('/')
    if (!fileName.endsWith(".class", true)
     || fileName.contains("$")
     || fileName.contains("package-info.class")
    ) {
     toRemove.add(sp)
    }
   }
  }
  toDecompile.removeAll(toRemove)
  toDecompile.addAll(toAdd)
 }
 //Decompile
 val decompiler = JavaDecompileProxy(flags)
 val outputPath = sanitize(paths[1])
 val output = File(outputPath)
 if (output.exists()) {
  output.delete()
 }
 output.mkdirs()
// val start = System.currentTimeMillis()
 //TODO fix path matching for output
 toDecompile
  .parallelStream()
  .forEach {
   val rootParentPath = rootAbsolutePath.replaceAfterLast("/", "")
   val sourcePath = it
    .replace(rootAbsolutePath, "$rootParentPath$outputPath")
    .replace(".class", ".java")
   val source = File(sourcePath)
   if (source.exists()) {
    source.delete()
   } else {
    val sourceParentPath = sourcePath.replaceAfterLast("/", "")
    val sourceParentDir = File(sourceParentPath)
    if (!sourceParentDir.exists()) {
     sourceParentDir.mkdirs()
    }
   }
   source.createNewFile()
   Files.write(
    source.toPath(),
    decompiler.decompile(it).toByteArray(),
    StandardOpenOption.WRITE
   )
  }
// println("Decompiled ${toDecompile.size} files in ${System.currentTimeMillis() - start}ms!")
}

fun sanitize(input: String): String {
 return input.replace('\\', '/')
}