package org.mcdh.jda.consts

class JdaPrimitiveConstant: JdaPooledConstant {
 var index: Int = 0
 var value: Any = object {}
 var isArray: Boolean = false

 constructor(type: Int, value: Any): super(type) {
  this.value = value
 }

 constructor(type: Int, index: Int): super(type) {
  this.index = index
 }

 private fun initConstant() {
  if (type == 7) {
   val className = getString()
   isArray = className.isNotEmpty() && className[0] == '['
  }
 }

 fun getString(): String {
  return value as String
 }

 override fun resolveConstant(cp: IConstantPool) {
  if (type == 7 || type == 8 || type == 16) {
   value = cp.getPrimitiveConstant(index).getString()
   initConstant()
  }
 }

 override fun equals(other: Any?): Boolean {
  if (other is JdaPrimitiveConstant) {
   return type == other.type
    && isArray == other.isArray
    && value == other.value
  }
  return false
 }
}