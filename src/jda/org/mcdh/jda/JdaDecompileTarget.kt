package org.mcdh.jda

import org.jetbrains.java.decompiler.main.ClassesProcessor
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger
import org.jetbrains.java.decompiler.main.extern.IIdentifierRenamer
import org.jetbrains.java.decompiler.modules.renamer.ConverterHelper
import org.jetbrains.java.decompiler.modules.renamer.IdentifierConverter
import org.jetbrains.java.decompiler.modules.renamer.PoolInterceptor
import org.jetbrains.java.decompiler.struct.StructContext
import org.jetbrains.java.decompiler.struct.lazy.LazyLoader
import java.io.File

//ContextUnit
//TODO add thread name
class JdaDecompileTarget constructor(val context: JdaDecompilerContext, val targets: Map<String, File>): Thread() {
// val targets: Map<String, File>
// val structContext: StructContext
// val classesProcessor: ClassesProcessor
// val helper: IIdentifierRenamer?
// val converter: IdentifierConverter?

 //TODO
 override fun run() {
  val structContext = StructContext(context.saver, context, LazyLoader(context))
  val classesProcessor = ClassesProcessor(structContext)
  val helper: IIdentifierRenamer?
  val converter: IdentifierConverter?
  //Add targets to struct context
  targets.forEach {
   structContext.addSpace(it.value, true)
  }
  //Add libraries to struct context
  context.classpath.forEach {
   structContext.addSpace(it, false)
  }
  //Process renaming options
  if ("1" == context.options["ren"] ?: false) {
   val getHelper = fun(): IIdentifierRenamer {
    val urc = context.options["urc"]
    if (urc != null) {
     try {
      return javaClass
       .classLoader
       .loadClass(urc as String)
       .getDeclaredConstructor()
       .newInstance() as IIdentifierRenamer
     } catch(t: Throwable) {
      context.logger.writeMessage("Could not load renamer '${urc as String}'", IFernflowerLogger.Severity.WARN, t)
     }
    }
    return ConverterHelper()
   }
   helper = getHelper()
   converter = IdentifierConverter(structContext, helper, PoolInterceptor())
  } else {
   helper = null
   converter = null
  }
 }
}