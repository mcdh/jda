package org.mcdh.jda

import org.cliffc.high_scale_lib.NonBlockingHashMap
import org.jetbrains.java.decompiler.main.extern.IResultSaver
import java.util.jar.Manifest

class ResultSaver: IResultSaver {
// private var result: String = ""
// private var mapping: IntArray = IntArray(0)
 private var results = NonBlockingHashMap<String, Pair<String, IntArray>>()

 override fun saveFolder(p0: String?) { /* unimplemented */ }

 override fun closeArchive(p0: String?, p1: String?) { /* unimplemented */ }

 override fun copyFile(p0: String?, p1: String?, p2: String?) { /* unimplemented */ }

 override fun copyEntry(p0: String?, p1: String?, p2: String?, p3: String?) { /* unimplemented */ }

 override fun saveClassEntry(p0: String?, p1: String?, p2: String?, p3: String?, p4: String?) { /* unimplemented */ }

 override fun createArchive(p0: String?, p1: String?, p2: Manifest?) { /* unimplemented */ }

 override fun saveDirEntry(p0: String?, p1: String?, p2: String?) { /* unimplemented */ }

 override fun saveClassFile(path: String, qualifiedName: String, entryName: String, content: String, mapping: IntArray) {
  println("Adding source for file: $path")
  results[path] = Pair(content, mapping)
//  this.result = content
//  this.mapping = mapping
 }

 //TODO Should sanitize?
 fun getResult(path: String): String {
  return results[path]?.first ?: "'$path' HAS NOT YET BEEN DECOMPILED OR WAS NOT A TARGET FOR DECOMPILATION!"
 }

 //TODO Should sanitize?
 fun getMapping(path: String): IntArray {
  return results[path]?.second ?: IntArray(0)
 }

 protected fun clearResults() {
  results.clear()
 }

// fun getResult(): String {
//  return result
// }
//
// fun getMapping(): IntArray {
//  return mapping
// }
}