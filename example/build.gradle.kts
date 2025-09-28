import de.alexanderwolz.xsd.generator.XsdJavaGenerator
import de.alexanderwolz.xsd.generator.task.XsdJavaGeneratorTask

buildscript {
    dependencies {
        //We need precompiled classes for the Generator to be used in Gradle
        classpath(fileTree(mapOf("dir" to "libs", "include" to listOf("xsd-generator-v*.jar"))))
        classpath("org.slf4j:slf4j-simple:2.0.17")
        classpath("jakarta.xml.bind:jakarta.xml.bind-api:4.0.2")
        classpath("org.glassfish.jaxb:jaxb-runtime:4.0.5")
        classpath("org.glassfish.jaxb:jaxb-xjc:4.0.5")
        classpath("org.jvnet.jaxb:jaxb-plugins:4.0.11") //equals, toString, hashcode
    }
}

plugins {
    kotlin("jvm")
}

group = "de.alexanderwolz"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val xjc: Configuration by configurations.creating
val xjcGenDir = layout.buildDirectory.dir("generated/sources/xjc/main/java")
val xjcSchemaFolder = "../schemas"

dependencies {
    implementation("org.glassfish.jaxb:jaxb-runtime:4.0.5")
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.2")
    implementation("org.jvnet.jaxb:jaxb-plugins:4.0.11")
    implementation("jakarta.annotation:jakarta.annotation-api:3.0.0")
    testImplementation(kotlin("test"))
}

sourceSets {
    named("main") {
        java.srcDir(xjcGenDir)
    }
}

tasks.named("compileJava") {
    dependsOn("generateJaxb")
}
tasks.named("compileKotlin") {
    dependsOn("generateJaxb")
}

tasks.register<XsdJavaGeneratorTask>("generateJaxb") {
    outputDir = xjcGenDir.get().asFile
    schemas = fileTree(xjcSchemaFolder) { include("*.xsd") }.files
    bindings = schemas.map { File(it.parent, "${it.nameWithoutExtension}.xjb.xml") }.filter { it.exists() }
    episodes = emptyList()
    catalog = null
    createEpisode = true
    flags = XsdJavaGenerator.Flags.values().toList()
    packageName = null
}

tasks.register("generateJaxbAlternative") {
    group = "generation"
    description = "Generates Java classes from XSD schemas"

    // Input/Output für Gradle Caching
    inputs.dir(xjcSchemaFolder)
    outputs.dir(xjcGenDir)

    doLast {
        val generator = XsdJavaGenerator(xjcGenDir.get().asFile)
        val schemas = fileTree(xjcSchemaFolder) { include("*.xsd") }.files
        val bindings = schemas.map { File(it.parent, "${it.nameWithoutExtension}.xjb.xml") }.filter { it.exists() }
        val episodes = emptyList<File>()
        val catalog = null
        val createEpisode = false
        val flags = XsdJavaGenerator.Flags.values().toList()
        val packageName = null
        logger.info("Generating from ${schemas.size} schema(s)")
        generator.generate(schemas, bindings, episodes, catalog, createEpisode, flags, packageName)
        logger.info("Successfully generated Java classes")
    }
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(22)
}