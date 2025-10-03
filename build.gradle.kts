import de.alexanderwolz.xsd.generator.XsdJavaGenerator
import de.alexanderwolz.xsd.generator.task.XsdJavaGeneratorTask

plugins {
    kotlin("jvm") version "2.2.10"
    id("java-library")
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
    id("maven-publish")
    jacoco
    signing
}

group = "de.alexanderwolz"
version = "1.1.0"

repositories {
    mavenCentral()
}

buildscript {
    dependencies {
        //We need precompiled classes for the Generator to be used in Gradle
        //classpath(fileTree(mapOf("dir" to "libs", "include" to listOf("xsd-generator-*.jar"))))
        classpath("de.alexanderwolz:xsd-generator:1.0.1")

        classpath("jakarta.xml.bind:jakarta.xml.bind-api:4.0.2")
        classpath("org.glassfish.jaxb:jaxb-runtime:4.0.5")
        classpath("org.glassfish.jaxb:jaxb-xjc:4.0.5")
        classpath("org.jvnet.jaxb:jaxb-plugins:4.0.11") //equals, toString, hashcode
    }
}

kotlin {
    jvmToolchain(22)
}

dependencies {
    implementation("de.alexanderwolz:commons-log:1.0.0")
    implementation("org.glassfish.jaxb:jaxb-xjc:4.0.5")
    compileOnly("jakarta.annotation:jakarta.annotation-api:3.0.0")
    compileOnly("org.jvnet.jaxb:jaxb-plugins:4.0.11")
    compileOnly(gradleApi())

    testImplementation(kotlin("test"))
    testImplementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.2")
    testImplementation("org.glassfish.jaxb:jaxb-runtime:4.0.5")
    testImplementation("org.glassfish.jaxb:jaxb-xjc:4.0.5")
    testImplementation("org.jvnet.jaxb:jaxb-plugins:4.0.11")
    testImplementation("jakarta.annotation:jakarta.annotation-api:3.0.0")
}

val xjcGenDir = fileTree("build/generated/sources/xjc/main/java").dir
val schemaFolder = fileTree("schemas").dir

sourceSets {
    named("main") {
        java.srcDir(xjcGenDir)
    }
}

//INFO: set org.gradle.logging.level=info (e.g. gradle.properties) for log output
tasks.register<XsdJavaGeneratorTask>("generateJaxb") {
    outputDir = xjcGenDir
    schemas = fileTree(schemaFolder) { include("*.xsd") }.files
    bindings = schemas.map { File(it.parent, "${it.nameWithoutExtension}.xjb.xml") }.filter { it.exists() }
    episodes = emptyList()
    catalog = null
    createEpisode = false
    flags = XsdJavaGenerator.Flags.values().toList()
    packageName = null
}

//INFO: set org.gradle.logging.level=info (e.g. gradle.properties) for log output
tasks.register("generateJaxbAlternative") {
    group = "generation"
    description = "Generates Java classes from XSD schemas"
    doLast {
        val generator = XsdJavaGenerator(xjcGenDir, encoding = Charsets.UTF_8, logger = logger)
        val schemas = fileTree(schemaFolder) { include("*.xsd") }.files
        val bindings = schemas.map { File(it.parent, "${it.nameWithoutExtension}.xjb.xml") }.filter { it.exists() }
        val episodes = emptyList<File>()
        val catalog = null
        val createEpisode = false
        val flags = XsdJavaGenerator.Flags.values().toList()
        val packageName = null
        generator.generate(schemas, bindings, episodes, catalog, createEpisode, flags, packageName)
    }
}

//INFO: set org.gradle.logging.level=info (e.g. gradle.properties) for log output
tasks.register("generateJaxbWithDependencies") {
    group = "generation"
    description = "Generates Java classes from XSD schemas"
    doLast {
        generate("complexParent_v6.xsd", listOf("articleListCollection_v3.xsd"))
    }
}

private fun generate(schema: String, deps: Collection<String> = emptyList()): Boolean {
    val generator = XsdJavaGenerator(xjcGenDir, encoding = Charsets.UTF_8, logger = logger)
    val schemas = listOf(File(schemaFolder, schema))
    val bindings = schemas.map { File(schemaFolder, "${it.nameWithoutExtension}.xjb.xml") }
    val dependencies = HashMap<File, Collection<File>>()
    deps.forEach {
        val depSchema = File(schemaFolder, it)
        val depBindings = listOf(File(depSchema.parent, "${depSchema.nameWithoutExtension}.xjb.xml"))
        dependencies[depSchema] = depBindings
    }
    val catalog = null
    val createEpisode = false
    val flags = XsdJavaGenerator.Flags.values().toList()
    val packageName = null
    return generator.generate(schemas, bindings, dependencies, catalog, createEpisode, flags, packageName)

}

tasks.named("compileJava") {
    dependsOn("generateJaxb")
}
tasks.named("compileKotlin") {
    dependsOn("generateJaxb")
}

tasks.jar {
    exclude("de/alexanderwolz/model/**")
    exclude("de/alexanderwolz/generated/**")
}

tasks.test {
    useJUnitPlatform()
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
            username.set(System.getenv("SONATYPE_USERNAME"))
            password.set(System.getenv("SONATYPE_PASSWORD"))
        }
    }
}
