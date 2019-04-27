package org.mcdh.jda.collectors

import org.jetbrains.java.decompiler.main.ClassesProcessor
import org.jetbrains.java.decompiler.struct.attr.StructGeneralAttribute
import org.jetbrains.java.decompiler.struct.attr.StructInnerClassesAttribute
import org.mcdh.jda.JdaDecompilerContext
import java.util.*

const val JAVA_LANG_PACKAGE = "java.lang"

class JdaImportCollector {
 var context: JdaDecompilerContext
 private val mapSimpleNames = mutableMapOf<String, String>()
 private val setNotImportedNames = hashSetOf<String>()
 private val setFieldNames = hashSetOf<String>()
 private val setInnerClassnames = hashSetOf<String>()
 private val currentPackageSlash: String
 private val currentPackagePoint: String

 constructor(context: JdaDecompilerContext, root: ClassesProcessor.ClassNode) {
  this.context = context
  val clName = root.classStruct.qualifiedName
  val index = clName.lastIndexOf(47.toChar())
  if (index > 0) {
   val packageName = clName.substring(0, index)
   currentPackageSlash = "$packageName/"
   currentPackagePoint = packageName.replace('/', '.')
  } else {
   currentPackageSlash = ""
   currentPackagePoint = ""
  }

  val classes = context.getClasses()
//  val queue = mutableListOf<String>()
  val queue = LinkedList<String>()
  var currentClass = root.classStruct

  while(currentClass != null) {
   if (currentClass.superClass != null) {
    queue.add(currentClass.superClass.string)
   }

   queue.addAll(currentClass.interfaceNames)
   currentClass.fields.forEach {
    setFieldNames.add(it.name)
   }
   val attribute = currentClass.getAttribute(StructGeneralAttribute.ATTRIBUTE_INNER_CLASSES)
   attribute?.entries?.forEach {
    if (it.enclosingName != null && it.enclosingName == currentClass.qualifiedName) {
     setInnerClassnames.add(it.simpleName)
    }
   }
   //TODO what is this doing
   do {
    currentClass = if (queue.isNotEmpty()) classes[queue.removeFirst()] else null
   } while(currentClass != null && queue.isNotEmpty())
  }
 }

 fun getShortNameInClassContext(classToName: String): String {
  val shortName = getShortname(classToName)
  return if (setFieldNames.contains(shortName)) classToName else shortName
 }

 fun getShortName(fullName: String): String {
  return getShortName(fullName, true)
 }

 fun getShortName(fullName: String, imported: Boolean): String {
  val node =
 }
}