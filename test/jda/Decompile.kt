import org.junit.Test
import org.mcdh.jda.JavaDecompileProxy
import kotlin.test.assertEquals

//var decompiler: IdeaDecompiler = IdeaDecompiler()
//
//fun decompile(f: File): java.lang.String {
// val vf: VirtualFile = LocalFileSystem.getInstance().findFileByIoFile(f)!!
// return decompiler.getText(vf).toString() as java.lang.String
//}

val decompiler = JavaDecompileProxy()

@Test
fun testDecompilation() {
 assertEquals(decompile(), "")
}

fun decompile(): String {
// for (path in args) {
//  println(decompiler.decompile(path as java.lang.String))
// }
 return ""
}