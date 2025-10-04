import de.alexanderwolz.xsd.generator.Flags
import de.alexanderwolz.xsd.generator.XsdJavaGenerator
import de.alexanderwolz.xsd.generator.task.XsdJavaGeneratorTask
import java.util.Base64

plugins {
    kotlin("jvm") version "2.2.10"
    id("java-library")
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
    id("maven-publish")
    jacoco
    signing
}

group = "de.alexanderwolz"
version = "1.2.0"

repositories {
    mavenLocal()
    mavenCentral()
}

java {
    withSourcesJar()
    withJavadocJar()
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(17)
}

buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
    }
    dependencies {
        //We need precompiled classes for the Generator to be used in Gradle
        classpath(fileTree(mapOf("dir" to "libs", "include" to listOf("xsd-generator-*.jar"))))
        //classpath("de.alexanderwolz:xsd-generator:1.1.0")
        classpath("de.alexanderwolz:commons-log:1.1.0")

        classpath("jakarta.xml.bind:jakarta.xml.bind-api:4.0.2")
        classpath("org.glassfish.jaxb:jaxb-runtime:4.0.6")
        classpath("org.glassfish.jaxb:jaxb-xjc:4.0.6")
        classpath("org.jvnet.jaxb:jaxb-plugins:4.0.11") //equals, toString, hashcode
    }
}

dependencies {
    implementation("de.alexanderwolz:commons-log:1.1.0")
    implementation("org.glassfish.jaxb:jaxb-xjc:4.0.5")
    compileOnly("jakarta.annotation:jakarta.annotation-api:3.0.0")
    compileOnly("org.jvnet.jaxb:jaxb-plugins:4.0.11")
    compileOnly(gradleApi())

    testImplementation(kotlin("test"))
    testImplementation("org.slf4j:slf4j-simple:2.0.17")
    testImplementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.2")
    testImplementation("org.glassfish.jaxb:jaxb-runtime:4.0.5")
    testImplementation("org.glassfish.jaxb:jaxb-xjc:4.0.5")
    testImplementation("org.jvnet.jaxb:jaxb-plugins:4.0.11")
    testImplementation("jakarta.annotation:jakarta.annotation-api:3.0.0")
}

val xjcGenDir = layout.buildDirectory.dir("generated/sources/xjc/main/java").get().asFile
val schemaFolder = layout.projectDirectory.dir("schemas").asFile

sourceSets {
    test {
        java {
            srcDir(xjcGenDir)
        }
    }
}

//INFO: set org.gradle.logging.level=info (e.g. gradle.properties) for log output
//TODO fix this: GitHub Runner complains about unknown definition
//  src-resolve: Cannot resolve the name 'articles:article' to a(n) 'type definition' component.
val generateJaxb = tasks.register<XsdJavaGeneratorTask>("generateJaxb") {
    outputDir = xjcGenDir
    schemas = fileTree(schemaFolder) { include("*.xsd") }.files
    bindings = schemas.map { File(it.parent, "${it.nameWithoutExtension}.xjb.xml") }.filter { it.exists() }
    episodes = emptyList()
    catalog = null
    createEpisode = false
    flags = Flags.values().toList()
    packageName = null
}


//INFO: set org.gradle.logging.level=info (e.g. gradle.properties) for log output
val generateJaxbAlternative = tasks.register("generateJaxbAlternative") {
    group = "generation"
    description = "Generates Java classes from XSD schemas"
    doLast {
        val generator = XsdJavaGenerator(xjcGenDir, encoding = Charsets.UTF_8)
        val schemas = fileTree(schemaFolder) { include("*.xsd") }.files
        val bindings = schemas.map { File(it.parent, "${it.nameWithoutExtension}.xjb.xml") }.filter { it.exists() }
        val episodes = emptyList<File>()
        val catalog = null
        val createEpisode = false
        val flags = Flags.values().toList()
        val packageName = null
        generator.generate(schemas, bindings, episodes, catalog, createEpisode, flags, packageName)
    }
}

//INFO: set org.gradle.logging.level=info (e.g. gradle.properties) for log output
val generateJaxbSimple = tasks.register("generateJaxbSimple") {
    group = "generation"
    description = "Generates Java classes from XSD schemas"
    doLast {
        val generator = XsdJavaGenerator(xjcGenDir, encoding = Charsets.UTF_8)
        generator.generate("complexParent_v6.xsd", listOf("articleListCollection_v3.xsd"), schemaFolder)
    }
}

tasks.compileTestKotlin {
    dependsOn(generateJaxbSimple)
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes(
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to "Alexander Wolz",
            "Built-By" to System.getProperty("user.name"),
            "Built-JDK" to System.getProperty("java.version"),
            "Created-By" to "Gradle ${gradle.gradleVersion}"
        )
    }
}

jacoco {
    toolVersion = "0.8.11"
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

//see also https://github.com/gradle-nexus/publish-plugin/tree/v2.0.0
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                name.set("HTTP Client")
                description.set("Sophisticated http client wrapper")
                url.set("https://github.com/alexanderwolz/http-client")
                licenses {
                    license {
                        name.set("AGPL-3.0")
                        url.set("https://www.gnu.org/licenses/agpl-3.0.html")
                    }
                }
                developers {
                    developer {
                        id.set("alexanderwolz")
                        name.set("Alexander Wolz")
                        url.set("https://www.alexanderwolz.de")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/alexanderwolz/http-client.git")
                    developerConnection.set("scm:git:ssh://git@github.com/alexanderwolz/http-client.git")
                    url.set("https://github.com/alexanderwolz/http-client")
                }
            }
        }
    }
}

signing {
    val signingKey = System.getenv("GPG_PRIVATE_KEY")
    val signingPassword = System.getenv("GPG_PASSPHRASE")

    if (signingKey != null && signingPassword != null) {
        logger.info("GPG credentials found in System")
        val decodedKey = String(Base64.getDecoder().decode(signingKey))
        useInMemoryPgpKeys(decodedKey, signingPassword)
        sign(publishing.publications["mavenJava"])
    } else {
        logger.info("No GPG credentials found in System, using cmd..")
        useGpgCmd()
        sign(publishing.publications["mavenJava"])
    }
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
