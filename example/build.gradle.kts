import de.alexanderwolz.xsd.generator.XsdJavaGenerator
import de.alexanderwolz.xsd.generator.task.XsdJavaGeneratorTask

buildscript {
    dependencies {
        //We need precompiled classes for the Generator to be used in Gradle
        classpath(fileTree(mapOf("dir" to "libs", "include" to listOf("generator-*.jar"))))
        classpath("jakarta.xml.bind:jakarta.xml.bind-api:4.0.2")
        classpath("org.glassfish.jaxb:jaxb-runtime:4.0.5")
        classpath("org.glassfish.jaxb:jaxb-xjc:4.0.5")
        classpath("org.jvnet.jaxb:jaxb-plugins:4.0.11")
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
        val allSchemas = fileTree(xjcSchemaFolder) { include("*.xsd") }.files
        val allBindings = allSchemas.map { File(it.parent, "${it.nameWithoutExtension}.xjb.xml") }.filter { it.exists() }

        logger.info("Generating from ${allSchemas.size} schema(s)")
        generator.generate(allSchemas, allBindings, emptyList(), null, false, null,null)
        logger.info("Successfully generated Java classes")
    }
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(22)
}