package org.mcdh.jda.consts

import org.jetbrains.java.decompiler.modules.renamer.PoolInterceptor
import org.jetbrains.java.decompiler.struct.gen.FieldDescriptor
import org.jetbrains.java.decompiler.struct.gen.MethodDescriptor
import org.jetbrains.java.decompiler.struct.gen.VarType

import org.jetbrains.java.decompiler.struct.gen.NewClassNameBuilder
import org.jetbrains.java.decompiler.util.DataInputFullStream
import org.mcdh.jda.JdaDecompilerContext
import java.io.DataInputStream
import java.util.*
import kotlin.collections.ArrayList

const val FIELD = 1
const val METHOD = 2

class JdaConstantPool constructor(val context: JdaDecompilerContext, private val din: DataInputStream): IConstantPool, NewClassNameBuilder {
 private val pool: MutableList<JdaPooledConstant?>
 private val interceptor: PoolInterceptor? = context.getPoolInterceptor()

 init {
  val size = din.readUnsignedShort()
  this.pool = ArrayList(size)
  val nextPass = arrayOf(BitSet(size), BitSet(size), BitSet(size))
  pool.add(null)
  var i = 1
  while (i < size) {
   //   val tag = din.readUnsignedByte().toByte()
   val lnk = fun (tag: Int) = JdaLinkConstant(tag, din.readUnsignedShort(), din.readUnsignedShort())
   when(val tag = din.readUnsignedByte()) {
    1 -> pool.add(JdaPrimitiveConstant(1, din.readUTF()))
    3 -> pool.add(JdaPrimitiveConstant(3, din.readInt()))
    4 -> pool.add(JdaPrimitiveConstant(4, din.readFloat()))
    5 -> {
     pool.add(JdaPrimitiveConstant(5, din.readLong()))
     pool.add(null)
     i += 1
    }
    6 -> {
     pool.add(JdaPrimitiveConstant(6, din.readDouble()))
     pool.add(null)
     i += 1
    }
    7, 8, 16 -> {
     pool.add(JdaPrimitiveConstant(tag, din.readUnsignedShort()))
     nextPass[0].set(i)
    }
    9, 10, 11, 18 -> {
     pool.add(lnk(tag))
     nextPass[1].set(i)
    }
    12 -> {
     pool.add(lnk(tag))
     nextPass[0].set(i)
    }
    15 -> {
     pool.add(JdaLinkConstant(tag, din.readUnsignedByte(), din.readUnsignedShort()))
     nextPass[2].set(i)
    }
   }
   i++
  }

  for (pass in nextPass) {
   var idx = 0

   do {
    idx = pass.nextSetBit(idx + 1)
    if (idx > 0) {
     (pool[idx] as JdaPooledConstant).resolveConstant(this)
    }
   } while (idx > 0)
  }
 }

 fun getClassElement(elementType: Int, className: String, nameIndex: Int, descriptorIndex: Int): Array<String> {
  var className = className
  var elementName = (getConstant(nameIndex) as JdaPrimitiveConstant).getString()
  var descriptor = (getConstant(descriptorIndex) as JdaPrimitiveConstant).getString()
  if (interceptor != null) {
   val oldClassname = interceptor.getOldName(className)
   if (oldClassname != null) {
    className = oldClassname
   }

   val newElement = interceptor.getName("$className $elementName $descriptor")
   if (newElement != null) {
    elementName = newElement.split(" ")[1]
   }

   val newDescriptor = buildNewDescriptor(elementType == 1, descriptor)
   if (newDescriptor != null) {
    descriptor = newDescriptor
   }
  }

  return arrayOf(elementName, descriptor)
 }

 fun getConstant(index: Int): JdaPooledConstant? {
  return pool[index]
 }

 override fun getPrimitiveConstant(index: Int): JdaPrimitiveConstant? {
  var cn = getConstant(index)
  if (cn != null && interceptor != null && cn.type == 7) {
   cn = cn as JdaPrimitiveConstant
   val newName = buildNewClassname(cn.getString())
   if (newName != null) {
    cn = JdaPrimitiveConstant(7, newName)
   }
  }
  return cn as JdaPrimitiveConstant?
 }

 override fun getLinkConstant(index: Int): JdaLinkConstant? {
  var ln = getConstant(index)
  if (ln != null && interceptor != null && (ln.type == 9 || ln.type == 10 || ln.type == 11)) {
   ln = ln as JdaLinkConstant
   val newClassName = buildNewClassname(ln.classname)
   val newElement = interceptor.getName("${ln.classname} ${ln.elementname} ${ln.descriptor}")
   val newDescriptor = buildNewDescriptor(ln.type == 9, ln.descriptor)
   if (newClassName != null || newElement != null || newDescriptor != null) {
    val className = newClassName ?: ln.classname
    val elementName = newElement?.split(" ")?.get(1) ?: ln.elementname
    val descriptor = newDescriptor ?: ln.descriptor
    ln = JdaLinkConstant(ln.type, className, elementName, descriptor)
   }
  }
  return ln as JdaLinkConstant?
 }

 override fun buildNewClassname(className: String): String? {
  val vt = VarType(className, true)
  val newName = interceptor?.getName(vt.value)
  if (newName == null) {
   return null
  } else {
   val builder = StringBuilder()
   if (vt.arrayDim > 0) {
    for (i in 0 until vt.arrayDim) {
     builder.append('[')
    }
    builder
     .append('L')
     .append(newName)
     .append(';')
   } else {
    builder.append(newName)
   }
   return builder.toString()
  }
 }

 private fun buildNewDescriptor(isField: Boolean, descriptor: String): String? {
  return if (isField) {
   FieldDescriptor
    .parseDescriptor(descriptor)
    .buildNewDescriptor(this)
  } else {
   MethodDescriptor
    .parseDescriptor(descriptor)
    .buildNewDescriptor(this)
  }
 }
}

fun skipPool(din: DataInputFullStream) {
 val size = din.readUnsignedShort()
 var i = 1
 while (i < size) {
  when(din.readUnsignedByte()) {
   1 -> din.readUTF()
   3, 4, 9, 10, 11, 12, 18 -> din.discard(4)
   5, 6 -> {
    din.discard(8)
    i += 1
   }
   7, 8, 16 -> din.discard(2)
   15 -> din.discard(3)
  }
  i++
 }
}