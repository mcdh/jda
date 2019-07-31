package org.mcdh.jda

import java.io.File
import java.nio.file.Files
import kotlin.system.exitProcess

//class Main constructor(private val options: Map<String, String>) {
// private class BadFix(flags: Map<String, String>): ClassLoader() {
//  companion object {
//   private val mdefineClass0 = ClassLoader::class.java.getDeclaredMethod(
//    "defineClass0",
//    String::class.java,
//    ByteArray::class.java,
//    Int::class.java,
//    Int::class.java,
//    ProtectionDomain::class.java
//   )
//
//   private val classCache = mutableMapOf<String, ByteArray>()
//
//   init {
//    mdefineClass0.isAccessible = true
//   }
//  }
//
//  private val currentThread: Thread = Thread.currentThread()
//  private val scl: ClassLoader
//  val decompileProxy: JavaDecompileProxy
//
//  init {
//   this.scl = currentThread.contextClassLoader
//   currentThread.contextClassLoader = this
//   this.decompileProxy = JavaDecompileProxy(flags)
////   val clazz = loadClass("org.mcdh.jda.JavaDecompileProxy") as Class<JavaDecompileProxy>
////   decompileProxy = clazz.constructors.iterator().next().newInstance(flags)!! as JavaDecompileProxy
//  }
//
//  fun close() {
//   currentThread.contextClassLoader = scl
//  }
//
//  override fun loadClass(name: String): Class<*> {
//   val normalizedName = name.toLowerCase()
//   val path = "${name.replace(".", "/")}.class"
////   if (normalizedName.contains("org.mcdh") || normalizedName.contains("org.jetbrains.java.decompiler")) {
//   if (normalizedName.contains("org.jetbrains")) {
//    val data = classCache[name] ?: super.getResourceAsStream(path)!!.readBytes()
//    classCache[name] = data
//    return mdefineClass0.invoke(this, name, data, 0, data.size, null) as Class<*>
//   }
//   return scl.loadClass(name)
//  }
// }
//
// fun decompile(path: String): String {
//  val badFix = BadFix(options)
//  val ret = badFix.decompileProxy.decompile(path)
//  badFix.close()
//  return ret
// }
//}

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
// val main = Main(flags)
 //Process input files
// val toDecompile = mutableListOf<String>()
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
    f.list().forEach {
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
 //NOTE: Reusing the same decompiler instance for multiple classes causes strange behaviour that results in inner
 //classes not being fully decompiled and generic signatures disappearing from the resulting source
// val decompiler = JavaDecompileProxy(flags)
 val outputPath = sanitize(paths[1])
 val output = File(outputPath)
 if (output.exists()) {
  output.delete()
 }
 output.mkdirs()
 toDecompile
//  .parallelStream()
  .stream()
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
   val decompiler = JavaDecompileProxy(flags)
   Files.write(
    source.toPath(),
    decompiler.decompile(it).toByteArray()
   )
  }
}

fun sanitize(input: String): String {
 return input.replace('\\', '/')
}