# XSD Generator

![GitHub release (latest by date)](https://img.shields.io/github/v/release/alexanderwolz/xsd-generator)
![GitHub](https://img.shields.io/github/license/alexanderwolz/xsd-generator)
![GitHub code size in bytes](https://img.shields.io/github/languages/code-size/alexanderwolz/xsd-generator)
![GitHub all releases](https://img.shields.io/github/downloads/alexanderwolz/xsd-generator/total?color=informational)

## 🧑‍💻 About

This repository provides generators for creating Java classes out of XSD schema files.

## 🛠️ Build
1. Create jar resource using ```./gradlew clean build```
2. Copy  ```/generator/build/libs/*.jar``` into your project
3. Use the parser directly or add the Gradle task to your build.gradle(.kts)

## 📦 Getting the latest release

You can pull the latest binaries from the central Maven repositories:

with Gradle
```kotlin
implementation("de.alexanderwolz:xsd-generator-client:1.5.1")
```
with Maven
```xml
<dependency>
  <groupId>de.alexanderwolz</groupId>
  <artifactId>xsd-generator</artifactId>
    <version>1.5.1</version>
</dependency>
```

## 🪄 Example

Simple schema file generation 
```kotlin
val generator = XsdJavaGenerator("build/generated/xjc")
val schema = File(schemaDir, "articleListCollection_v3.xsd")
val bindings = listOf(File(schemaDir, "${schema.nameWithoutExtension}.xjb.xml"))
val episodes = emptyList<File>()
val catalog = null
val createEpisode = true
val flags = Flags.DEFAULTS
val packageName = "com.domain.generated"
generator.generate(listOf(schema), bindings, episodes, catalog, createEpisode, flags, packageName)
```
Recursive auto resolving of nested schema files
```kotlin
val generator = XsdJavaGenerator.create(xjcGenDir, Charsets.UTF_8, customLogger)
generator.generateAutoResolve(
    "complexParent_v6.xsd",
    schemaFolder,
    useFilenameVersions = true,
    flags = Flags.values().toList()
)
```
Use as Gradle Task
```kotlin
tasks.register<XsdJavaGeneratorTask>("generateModel") {
    outputDir = xjcGenDir
    schemas = fileTree(schemaFolder) { include("article_v3.xsd", "complexParent_v6.xsd") }.files
    useFilenameVersions = true
    flags = Flags.values().toList()
}
```

- - -

Made with ❤️ in Bavaria
<br>
© 2025, <a href="https://www.alexanderwolz.de"> Alexander Wolz
