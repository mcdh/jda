# jda
An adapter API for JetBrains' Java-Decompiler version of FernFlower, written in Kotlin. 

## Building
Use the shipped gradle wrapper for build repeatability. On Windows, `./gradlew.bat build`, otherwise, `./gradlew build`.
The output artifact will be `build/libs/jda-X.X.X.jar`.

## Goals
 - Remove dependence on all IntelliJ libraries

## License
This project is licensed under the [M.I.T License](https://github.com/mcdh/jda/blob/master/LICENSE)