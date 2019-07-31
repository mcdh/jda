package org.mcdh.jda

import org.jetbrains.java.decompiler.main.extern.IResultSaver
import java.util.jar.Manifest

class ResultSaver: IResultSaver {
 private var result: String = ""
 private var mapping: IntArray = IntArray(0)

 override fun saveFolder(p0: String?) { /* unimplemented */ }

 override fun closeArchive(p0: String?, p1: String?) { /* unimplemented */ }

 override fun copyFile(p0: String?, p1: String?, p2: String?) { /* unimplemented */ }

 override fun copyEntry(p0: String?, p1: String?, p2: String?, p3: String?) { /* unimplemented */ }

 override fun saveClassEntry(p0: String?, p1: String?, p2: String?, p3: String?, p4: String?) { /* unimplemented */ }

 override fun createArchive(p0: String?, p1: String?, p2: Manifest?) { /* unimplemented */ }

 override fun saveDirEntry(p0: String?, p1: String?, p2: String?) { /* unimplemented */ }

 override fun saveClassFile(path: String, qualifiedName: String, entryName: String, content: String, mapping: IntArray) {
  this.result = content
  this.mapping = mapping
 }

 fun getResult(): String {
  return result
 }

 fun getMapping(): IntArray {
  return mapping
 }
}