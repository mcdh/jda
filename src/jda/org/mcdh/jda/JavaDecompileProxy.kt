package org.mcdh.jda

import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger
import java.security.ProtectionDomain

class JavaDecompileProxy @JvmOverloads constructor(
 val options: Map<String, String> = mapOf(),
 val classpath: List<String> = listOf(),
 val saver: ResultSaver = ResultSaver(),
 val logger: IFernflowerLogger = Logger()
) {

 private class FFClassLoader constructor(private val proxy: JavaDecompileProxy): ClassLoader() {
  companion object {
   private val mdefineClass0 = ClassLoader::class.java.getDeclaredMethod(
    "defineClass0",
    String::class.java,
    ByteArray::class.java,
    Int::class.java,
    Int::class.java,
    ProtectionDomain::class.java
   )

   private val classCache = mutableMapOf<String, ByteArray>()

   init {
    mdefineClass0.isAccessible = true
   }
  }

  private val currentThread: Thread = Thread.currentThread()
  private val scl: ClassLoader
  val decompiler: Decompiler

  init {
   this.scl = currentThread.contextClassLoader
   currentThread.contextClassLoader = this
   this.decompiler = Decompiler(proxy)
  }

  fun close() {
   currentThread.contextClassLoader = scl
  }

  override fun loadClass(name: String): Class<*> {
   val normalizedName = name.toLowerCase()
   val path = "${name.replace(".", "/")}.class"
//   if (normalizedName.contains("org.mcdh") || normalizedName.contains("org.jetbrains.java.decompiler")) {
   if (normalizedName.contains("org.jetbrains")) {
    val data = classCache[name] ?: super.getResourceAsStream(path)!!.readBytes()
    classCache[name] = data
    return mdefineClass0.invoke(this, name, data, 0, data.size, null) as Class<*>
   }
   return scl.loadClass(name)
  }
 }

 fun decompile(path: String): String {
  if (Thread.currentThread().javaClass == FFClassLoader::class.java) {
   throw RuntimeException("Deadlock detected in decompiler!")
  }
  val loader = FFClassLoader(this)
  val ret = loader.decompiler.decompile(path)
  loader.close()
  return ret
 }
}