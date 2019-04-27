package org.mcdh.jda.collectors

import org.jetbrains.java.decompiler.main.collectors.BytecodeMappingTracer
import org.jetbrains.java.decompiler.util.TextBuffer
import org.mcdh.jda.JdaDecompilerContext
import java.util.*

class JdaBytecodeSourceMapper constructor(val context: JdaDecompilerContext) {
 private var offset_total = 0
 private val mapping = mutableMapOf<String, MutableMap<String, MutableMap<Int, Int>>>()
 private val linesMapping = mutableMapOf<Int, Int>()
 private val unmappedLines = TreeSet<Int>()

 fun addMapping(className: String, methodName: String, bytecodeOffset: Int, sourceLine: Int) {
  val class_mapping = mapping.computeIfAbsent(className) {
   return@computeIfAbsent LinkedHashMap()
  }
  val method_mapping = class_mapping.computeIfAbsent(methodName) {
   return@computeIfAbsent HashMap()
  }
  method_mapping.putIfAbsent(bytecodeOffset, sourceLine)
 }

 fun addTracer(className: String, methodName: String, tracer: BytecodeMappingTracer) {
  tracer.mapping.forEach {
   addMapping(className, methodName, it.key, it.value)
  }

  linesMapping.putAll(tracer.originalLinesMapping)
  unmappedLines.addAll(tracer.unmappedLines)
 }

 fun dumpMapping(buffer: TextBuffer, offsetsToHex: Boolean) {
  if (mapping.isNotEmpty() || linesMapping.isNotEmpty()) {
   val lineSeparator = context.getNewLineSeparator()
   mapping.forEach { class_entry ->
    val class_mapping = class_entry.value
    buffer.append("class '${class_entry.value}' {$lineSeparator")
    var is_first_method = true
    class_mapping.forEach { method_entry ->
     val method_mapping = method_entry.value
     if (!is_first_method) {
      buffer.appendLineSeparator()
     }

     buffer
      .appendIndent(1)
      .append("method '${method_entry.key}' {$lineSeparator")
     val lstBytecodeOffsets = mutableListOf<Int>()
     lstBytecodeOffsets.addAll(method_mapping.keys)
     lstBytecodeOffsets.sort()
     lstBytecodeOffsets.forEach {
      val line = method_mapping[it]!!
      val strOffset = if (offsetsToHex) Integer.toHexString(it) else line.toString()
      buffer
       .appendIndent(2)
       .append(strOffset)
       .appendIndent(2)
       .append("${line + offset_total}$lineSeparator")
     }
     buffer
      .appendIndent(1)
      .append("}")
      .appendLineSeparator()
     is_first_method = false
    }
    //TODO why 2 line separators
    buffer
     .append("}")
     .appendLineSeparator()
     .appendLineSeparator()
   }
   buffer
    .append("Lines mapping: ")
    .appendLineSeparator()
   val sorted = TreeMap(linesMapping)
   sorted.forEach {
    buffer
     .append(it.key)
     .append(" <-> ")
     .append(it.value + offset_total + 1)
     .appendLineSeparator()
   }
   if (unmappedLines.isNotEmpty()) {
    buffer
     .append("Not mapped:")
     .appendLineSeparator()
    unmappedLines.forEach {
     if (!linesMapping.containsKey(it)) {
      buffer
       .append(it)
       .appendLineSeparator()
     }
    }
   }
  }
 }

 fun addTotalOffset(offset_total: Int) {
  this.offset_total += offset_total
 }

 fun getOriginalLinesMapping(): IntArray {
  val res = IntArray(linesMapping.size + 2)
  var i = 0
  for (e in linesMapping) {
   i += 2
   res[i] = e.key
   unmappedLines.remove(e.key)
   res[i + 1] = e.value + this.offset_total + 1
  }
  return res
 }
}