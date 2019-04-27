package org.mcdh.jda

import org.jetbrains.java.decompiler.code.CodeConstants
import org.jetbrains.java.decompiler.main.extern.IIdentifierRenamer
import org.jetbrains.java.decompiler.struct.StructClass
import org.jetbrains.java.decompiler.util.TextBuffer

import org.jetbrains.java.decompiler.main.ClassesProcessor.*
import org.jetbrains.java.decompiler.main.collectors.ImportCollector

class JdaClassesProcessor constructor(val context: JdaDecompilerContext): CodeConstants {
 class Inner constructor(val simpleName: String, val type: Int, val accessFlags: Int) {
  fun equal(i: Inner): Boolean {
   return type == i.type
    && accessFlags == i.accessFlags
    && equals(i as Any)
  }
 }

 fun loadClasses(renamer: IIdentifierRenamer) {
  val mapInnerClasses = mutableMapOf<String, Inner>()
  val mapNestedClassReferences = mutableMapOf<String, Set<String>>()
  val mapEnclosingClassReferences = mutableMapOf<String, String>()
  val bDecompileInner = context.options["din"]?.equals("1") ?: false
  val verifyAnonymousClasses = context.options["vac"]?.equals("1") ?: false

 }

 fun isAnonymous(): Boolean {
  return false
 }

 fun writeClass(cl: StructClass, buffer: TextBuffer) {

 }

 fun getMapRootClasses(): Map<String, ClassNode> {

 }
}

fun initWrappers(node: ClassNode) {

}

fun addClassnameToImport(node: ClassNode, imp: ImportCollector) {

}

fun destroyWrappers(node: ClassNode) {

}

fun