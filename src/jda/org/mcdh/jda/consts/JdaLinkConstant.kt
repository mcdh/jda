package org.mcdh.jda.consts

import org.jetbrains.java.decompiler.struct.consts.LinkConstant
import java.lang.IllegalArgumentException

class JdaLinkConstant: JdaPooledConstant {
 var index1: Int = 0
 var index2: Int = 0
 var classname: String = ""
 var elementname: String = ""
 var descriptor: String = ""

 constructor(type: Int, classname: String, elementname: String, descriptor: String): super(type) {
  this.classname = classname
  this.elementname = elementname
  this.descriptor = descriptor
  initConstant()
 }

 constructor(type: Int, index1: Int, index2: Int): super(type) {
  this.index1 = index1
  this.index2 = index2
 }

 private fun initConstant() {
  if (type == 10 || type == 11 || type == 18 || type == 15) {
   val parenth = descriptor.indexOf(41.toChar())
   if (descriptor.length < 2 || parenth < 0 || descriptor[0] != '(') {
    throw IllegalArgumentException("Invalid descriptor: $descriptor")
   }
  }
 }

 override fun resolveConstant(cp: IConstantPool) {
  if (type == 12) {
   elementname = cp.getPrimitiveConstant(index1).getString()
   descriptor = cp.getPrimitiveConstant(index2).getString()
  } else {
   var nametype: JdaLinkConstant
   if (type == 15) {
    nametype = cp.getLinkConstant(index2)
    classname = nametype.classname
   } else {
    if (type != 18) {
     classname = cp.getPrimitiveConstant(index1).getString()
    }
    nametype = cp.getLinkConstant(index2)
   }
   elementname = nametype.elementname
   descriptor = nametype.descriptor
  }
  initConstant()
 }

 override fun equals(other: Any?): Boolean {
  if (other is JdaLinkConstant) {
   val cn = other as LinkConstant
   return type == cn.type
    && elementname == cn.elementname
    && descriptor == cn.descriptor
    && (type != 12 || classname == cn.classname)
  }
  return false
 }
}