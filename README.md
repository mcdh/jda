# jda
An adapter API for JetBrains' Java-Decompiler version of FernFlower, written in Kotlin. 

## Building
The artifact cannot legally be distributed, therefore you must build the application locally in order to use it. To build the application and produce a system-wide copy that can be used in other projects, run the following commands:
```sh
git clone https://github.com/mcdh/jda.git
cd jda/
./gradlew build
```
The artifact will automatically be published to your local maven cache for further use in other projects.

## jda as a Dependency
To include jda in your project simply base your gradle configuration on the following:
```groovy
repositories {
 mavenLocal()
 mavenCentral()
}

dependencies {
 compile 'org.mcdh:jda:0.0.1'
}
```

## Usage
There is a single entry point in `org.mcdh.jda.JavaDecompileProxy`. To preserve interoperability with Java, `java.lang.String` was preferred over the native Kotlin type.
#### Kotlin
```kotlin
import org.mcdh.jda.JavaDecompileProxy

fun main(args: Array<String>) {
 val decompiler = JavaDecompileProxy()
 val decompiled: String = decompiler.decompile("/path/to/clazz.class" as java.lang.String) as String
}
```
#### Java
```java
import org.mcdh.jda.JavaDecompileProxyKt;

class Example {
 public static void main(String[] args) {
  final JavaDecompileProxyKt decompiler = new JavaDecompileProxyKt();
  decompiler.decompile("/path/to/clazz.class");
 }
}
```

## License
This project is licensed under the [M.I.T License](https://github.com/mcdh/jda/blob/master/LICENSE)