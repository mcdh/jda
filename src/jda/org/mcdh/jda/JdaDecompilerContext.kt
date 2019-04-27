package org.mcdh.jda

import org.jetbrains.java.decompiler.main.extern.*
import org.jetbrains.java.decompiler.modules.renamer.PoolInterceptor
import org.jetbrains.java.decompiler.struct.IDecompiledData
import org.jetbrains.java.decompiler.struct.StructClass
import org.mcdh.jda.collectors.JdaBytecodeSourceMapper
import java.io.File

class JdaDecompilerContext: IDecompiledData, IBytecodeProvider {
 val options: MutableMap<String, Any>
 val classpath: List<File>
 val saver: ResultSaver
 val logger: IFernflowerLogger

 //From StructContext
 private val units = mutableMapOf<String, JdaContextUnit>()
 private val classes = mutableMapOf<String, StructClass>()

 private val decompileTargets = mutableListOf<JdaDecompileTarget>()

 //From IBytecodeProvider
 val files = mutableMapOf<String, File>()

 //From DecompileContext
 private val poolInterceptor = PoolInterceptor()
 private val bytecodeSourceMapper: JdaBytecodeSourceMapper

 @JvmOverloads constructor(
  options: Map<String, Any>?,
  classpath: List<File> = listOf(),
  targets: List<File> = listOf(),
  saver: ResultSaver = ResultSaver(),
  logger: IFernflowerLogger = JdaLogger()
 ) {
  this.options = mutableMapOf()
  this.classpath = classpath
  this.saver = saver
  this.logger = logger
  this.options.putAll(fernflowerDefaultOptions())
  if (options != null) {
   this.options.putAll(options)
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

  //Construct targets
  for (target in targets) {
   addSource(target)
  }
 }

// //For option access
// operator fun get(s: String): Any? {
//  return options[s]
// }

 fun addSource(target: File) {
  if (target.extension != "class") {
   logger.writeMessage("'$target' is not a valid class file!", IFernflowerLogger.Severity.WARN)
   return
  }
  val targets = findSubclasses(target)
  files.putAll(targets)
  decompileTargets.add(JdaDecompileTarget(this, targets))
 }

 //Blocking
 fun decompile() {
  if (decompileTargets.isEmpty()) {
   logger.writeMessage("Nothing to decompile!", IFernflowerLogger.Severity.WARN)
   return
  }
  //TODO Consume current targets and make JdaDecompileTarget instances sleep when finished
  val running = mutableListOf<JdaDecompileTarget>()
  val processors = Runtime.getRuntime().availableProcessors()
//  while () {
//   for (i in 1..processors) {
//    val target = decompileTargets[0]
//    running.add(target)
//    decompileTargets.remove(target)
//   }
//   sleep(1000)
//  }
 }

 //From StructContext
 fun getClasses(): Map<String, StructClass> {
  return classes
 }

 //IDecompiledData
 override fun getClassContent(p0: StructClass?): String? {
  return ""
 }

 //IDecompiledData
 override fun getClassEntryName(p0: StructClass?, p1: String?): String? {
  return ""
 }

 //IBytecodeProvider
 override fun getBytecode(externalPath: String?, internalPath: String?): ByteArray {
  val path = externalPath!!.replace('\\', '/')
  val file = files[path]
  if (file != null) {
   return file.readBytes()
  } else {
   throw AssertionError("$path not in ${files.keys}")
  }
 }

 //DecompilerContext
 fun getNewLineSeparator(): String {
  return if (options["nls"]?.equals("1") == true) "\n" else "\r\n"
 }

 fun getPoolInterceptor(): PoolInterceptor {
  return poolInterceptor
 }
}