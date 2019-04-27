package org.mcdh.jda.consts

interface IConstantPool {
 fun getPrimitiveConstant(index: Int): JdaPrimitiveConstant?
 fun getLinkConstant(index: Int): JdaLinkConstant?
}