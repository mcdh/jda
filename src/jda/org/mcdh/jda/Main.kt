package org.mcdh.jda

import com.intellij.openapi.vfs.LocalFileSystem
import org.jetbrains.java.decompiler.IdeaDecompiler
import com.intellij.openapi.vfs.VirtualFile

import java.io.File
import java.lang.String

var decompiler: IdeaDecompiler = IdeaDecompiler()

//fun main(args: Array<String>) {
// println("Example")
//}

fun decompile(f: File): String {
 val vf: VirtualFile = LocalFileSystem.getInstance().findFileByIoFile(f)!!
 return decompiler.getText(vf).toString() as String
}
