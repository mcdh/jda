package org.mcdh.jda.consts

import org.jetbrains.java.decompiler.code.CodeConstants

abstract class JdaPooledConstant constructor(val type: Int): CodeConstants {
 abstract fun resolveConstant(cp: IConstantPool)
}