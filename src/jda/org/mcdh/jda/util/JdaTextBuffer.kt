package org.mcdh.jda.util

import org.mcdh.jda.JdaDecompilerContext
import java.util.*

class JdaTextBuffer {
 val context: JdaDecompilerContext
 private val builder: StringBuilder
 private val lineSeparator: String
 private val indent: String
 private var lineToOffsetMapping: MutableMap<Int, Int>? = null
 private var lineMapping: MutableMap<Int, MutableSet<Int>>? = null

 constructor(context: JdaDecompilerContext, builder: StringBuilder) {
  this.context = context
  this.builder = builder
  this.lineSeparator = context.getNewLineSeparator()
  this.indent = context.options["ind"] as String
 }

 constructor(context: JdaDecompilerContext) : this(context, StringBuilder())

 constructor(context: JdaDecompilerContext, size: Int): this(context, StringBuilder(size))

 constructor(context: JdaDecompilerContext, text: String): this(context, StringBuilder(text))

 fun append(s: String): JdaTextBuffer {
  builder.append(s)
  return this
 }

 fun append(ch: Char): JdaTextBuffer {
  builder.append(ch)
  return this
 }

 fun append(i: Int): JdaTextBuffer {
  builder.append(i)
  return this
 }

 fun appendLineSeparator(): JdaTextBuffer {
  builder.append(context.getNewLineSeparator())
  return this
 }

 fun appendIndent(length: Int): JdaTextBuffer {
  var len = length
  while(len-- > 0) {
   builder.append(indent)
  }
  return this
 }

 fun prepend(s: String): JdaTextBuffer {
  builder.insert(0, s)
  shiftMapping(s.length)
  return this
 }

 fun enclose(left: String, right: String): JdaTextBuffer {
  prepend(left)
  append(right)
  return this
 }

 fun containsOnlyWhitespace(): Boolean {
  for (i in 0 until builder.length) {
   if (builder[i] != ' ') {
    return false
   }
  }
  return true
 }

 override fun toString(): String {
  val original = builder.toString()
  if (lineToOffsetMapping?.isEmpty() != true) {
   val ltom = lineToOffsetMapping!!
   val res = StringBuilder()
   val srcLines = original.split(lineSeparator)
   var currentLineStartOffset = 0
   var currentLine = 0
   var previousMarkLine = 0
   var dumpedLines = 0
   val linesWithMarks = mutableListOf<Int>()
   linesWithMarks.addAll(ltom.keys)
   linesWithMarks.sort()

   while (true) {
    linesWithMarks.forEach { markLine ->
     val markOffset = lineToOffsetMapping!![markLine]!!
     for (i in currentLine until srcLines.size) {
      currentLine = i
      val line = srcLines[currentLine]
      val lineEnd = currentLineStartOffset + line.length + lineSeparator.length
      if (markOffset <= lineEnd) {
       val requiredLine = markLine - 1
       val linesToAdd = requiredLine - dumpedLines
       dumpedLines = requiredLine
       appendLines(res, srcLines, previousMarkLine, currentLine, linesToAdd)
       previousMarkLine = currentLine
       break
      }

      currentLineStartOffset = lineEnd
     }
    }

    if (previousMarkLine < srcLines.size) {
     appendLines(res, srcLines, previousMarkLine, srcLines.size, srcLines.size - previousMarkLine)
    }
    return res.toString()
   }
  } else {
   return if (lineMapping != null) {
    addOriginalLineNumbers()
   } else {
    original
   }
  }
 }

 private fun addOriginalLineNumbers(): String {
  val sb = StringBuilder()
  var lineStart = 0
  var count = 0
  var lineEnd: Int
  val length = lineSeparator.length

  do {
   lineEnd = builder.indexOf(lineSeparator, lineStart)
   lineStart = lineEnd + length
   count += 1
   sb.append(builder.substring(lineStart, lineEnd))
   val integers: Set<Int> = lineMapping?.get(count) ?: emptySet()
   if (integers.isNotEmpty()) {
    sb.append("//")
    integers.forEach {
     sb.append(' ').append(it)
    }
   }
   sb.append(lineSeparator)
  } while(lineEnd > 0)

  if (lineStart < builder.length) {
   sb.append(builder.substring(lineStart))
  }

  return sb.toString()
 }

 private fun appendLines(res: StringBuilder, srcLines: List<String>, from: Int, to: Int, requiredLineNumber: Int) {
  if (to - from > requiredLineNumber) {
   val strings = compactLines(srcLines.subList(from, to), requiredLineNumber)
   var separatorsRequired = requiredLineNumber - 1
   strings.forEach { s ->
    res.append(s)
    if (separatorsRequired-- > 0) {
     res.append(lineSeparator)
    }
   }
   res.append(lineSeparator)
  } else if (to - from <= requiredLineNumber) {
   for (i in from until to) {
    res.append(srcLines[i]).append(lineSeparator)
   }
   for (i in 0 until (requiredLineNumber - to + from)) {
    res.append(lineSeparator)
   }
  }
 }

 fun length(): Int {
  return builder.length
 }

 fun setStart(position: Int) {
  builder.delete(0, position)
  shiftMapping(-position)
 }

 fun setLength(position: Int) {
  builder.setLength(position)
  if (lineToOffsetMapping != null) {
   val newMap = mutableMapOf<Int, Int>()
   lineToOffsetMapping?.forEach {
    if (it.value <= position) {
     newMap[it.key] = it.value
    }
   }
   lineToOffsetMapping = newMap
  }
 }

 fun append(buffer: JdaTextBuffer): JdaTextBuffer {
  if (buffer.lineToOffsetMapping != null && buffer.lineToOffsetMapping!!.isNotEmpty()) {
   checkMapCreated()
   buffer.lineToOffsetMapping!!.forEach { (k, v) ->
    lineToOffsetMapping!!.put(k, v + builder.length)
   }
  }

  builder.append(buffer.builder)
  return this
 }

 private fun shiftMapping(shiftOffset: Int) {
  if (lineToOffsetMapping != null) {
   val newMap = mutableMapOf<Int, Int>()
   lineToOffsetMapping!!.forEach {
    var newValue = it.value
    if (newValue >= 0) {
     newValue += shiftOffset
    }
    if (newValue >= 0) {
     newMap[it.key] = newValue
    }
   }
   lineToOffsetMapping = newMap
  }
 }

 private fun checkMapCreated() {
  if (lineToOffsetMapping == null) {
   lineToOffsetMapping = mutableMapOf()
  }
 }

 fun countLines(): Int {
  return countLines(0)
 }

 fun countLines(from: Int): Int {
  return count(lineSeparator, from)
 }

 fun count(substring: String, from: Int): Int {
  var count = 0
  var length = substring.length
  var p = from
  do {
   p = builder.indexOf(substring, p)
   p += length
   count += 1
  } while (p > 0)
  return count
 }

 fun dumpOriginalLineNumbers(lineMapping: IntArray) {
  //TODO this function does nothing?
  if (lineMapping.isNotEmpty()) {
   this.lineMapping = mutableMapOf()
   for (i in 0 until lineMapping.size step 2) {
    val key = lineMapping[i + 1]
    val existing = this.lineMapping!!.computeIfAbsent(key) {
     return@computeIfAbsent TreeSet()
    }
    existing.add(lineMapping[i])
   }
  }
 }
}

private fun compactLines(srcLines: List<String>, requiredLineNumber: Int): List<String> {
 if (srcLines.size >= 2 && srcLines.size > requiredLineNumber) {
  val res = mutableListOf<String>()
  res.addAll(srcLines)
  for (i in res.size - 1 downTo 0) {
   val s = res[i]
   if (s.trim() == "{" || s.trim() == "}") {
    res[i - 1] = res[i - 1] + s
    res.removeAt(i)
   }

   if (res.size <= requiredLineNumber) {
    return res
   }
  }

  for (i in res.size - 1 downTo 0) {
   val s = res[i]
   if (s.trim().isEmpty()) {
    res[i - 1] = res[i - 1] + s
    res.removeAt(i)
   }

   if (res.size <= requiredLineNumber) {
    return res
   }
  }
 }
 return srcLines
}