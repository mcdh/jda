package org.mcdh.jda

import org.jetbrains.java.decompiler.struct.StructClass
import org.jetbrains.java.decompiler.struct.lazy.LazyLoader
import org.jetbrains.java.decompiler.struct.lazy.LazyLoader.Link
import java.util.jar.Manifest

const val TYPE_FOLDER = 0
const val TYPE_JAR = 1
const val TYPE_ZIP = 2

class JdaContextUnit @JvmOverloads constructor(
 val context: JdaDecompilerContext,
 val processor: JdaClassesProcessor,
 private val type: Int = 0,
 private val archivePath: String? = null,
 private val filename: String = "",
 private val own: Boolean = true
) {

 private val classEntries = mutableListOf<String>()
 private val dirEntries = mutableListOf<String>()
 private val otherEntries = mutableListOf<Array<String>>()
 private var classes = mutableListOf<StructClass>()
 private var manifest: Manifest? = null

 //Load
 init {

 }

 fun addClass(cl: StructClass, entryName: String) {
  classes.add(cl)
  classEntries.add(entryName)
 }

 fun addDirEntry(entry: String) {
  dirEntries.add(entry)
 }

 fun addOtherDirEntry(fullPath: String, entry: String) {
  otherEntries.add(arrayOf(fullPath, entry))
 }

 fun reload(loader: LazyLoader) {
  val lstClasses = mutableListOf<StructClass>()
  classes.forEach { cl ->
   val oldName = cl.qualifiedName
   val din = loader.getClassStream(oldName)
   //TODO exception handling
   val newCl = din.use {
    StructClass(din, cl.isOwn, loader)
   }
   lstClasses.add(newCl)
   val lnk = loader.getClassLink(oldName)
   loader.removeClassLink(oldName)
   loader.addClassLink(newCl.qualifiedName, lnk)
  }
  classes = lstClasses
 }

 fun save() {
  when(type) {
   TYPE_FOLDER -> {
    context.saver.saveFolder(filename)
    otherEntries.forEach { a ->
     context.saver.copyFile(a[0], filename, a[1])
    }
    for (i in 0 until classes.size) {
     val cl = classes[i]
     val entryName = context.getClassEntryName(cl, classEntries[i])
     if (entryName != null) {
      val mapping: IntArray? = if (context.options["bsm"]?.equals("1") == true) {
       context.getBytecodeSourceMapper().getOriginalLinesMapping()
      } else {
       null
      }
     }

    }
   }
   TYPE_JAR, TYPE_ZIP -> {

   }
  }
 }

 fun setManifest(manifest: Manifest) {
  this.manifest = manifest
 }

 fun isOwn(): Boolean {
  return own
 }

 fun getClasse(): List<StructClass> {
  return classes
 }
}