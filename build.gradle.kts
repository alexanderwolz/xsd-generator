import de.alexanderwolz.xsd.generator.Flags
import de.alexanderwolz.xsd.generator.XsdJavaGenerator
import de.alexanderwolz.xsd.generator.task.XsdJavaGeneratorTask
import java.util.*
import kotlin.collections.toList

plugins {
    kotlin("jvm") version "2.2.10"
    id("java-library")
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
    id("maven-publish")
    jacoco
    signing
}

group = "de.alexanderwolz"
version = "1.5.1"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://repo1.maven.org/maven2")
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

dependencies {
    implementation("de.alexanderwolz:commons-util:1.4.6")
    implementation("org.glassfish.jaxb:jaxb-xjc:4.0.6")
    implementation("org.glassfish.jaxb:jaxb-runtime:4.0.6")
    compileOnly(gradleApi())

    testImplementation(kotlin("test"))
    testImplementation(gradleTestKit())
    testImplementation("org.slf4j:slf4j-simple:2.0.17")
    testImplementation("org.jvnet.jaxb:jaxb-plugins:4.0.11") //equals, toString, hashcode
    testImplementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.2")
    testImplementation("jakarta.annotation:jakarta.annotation-api:3.0.0") //annotations
}

val xjcGenDir = layout.buildDirectory.dir("generated/sources/xjc/main/java").get().asFile
val schemaFolder = layout.projectDirectory.dir("schemas").asFile
val customLogger = de.alexanderwolz.commons.log.Logger(logger) { println(it.message) }

sourceSets {
    test {
        java {
            srcDir(xjcGenDir)
        }
    }
}

val generateJaxb = tasks.register("generateJaxb") {
    group = "generation"
    description = "Generates Java classes from XSD schemas"
    doLast {
        logger.lifecycle("Executing \"generateJaxb\"")
        val generator = XsdJavaGenerator.create(xjcGenDir, encoding = Charsets.UTF_8, customLogger)
        generator.generateAutoResolve(
            "complexParent_v6.xsd",
            schemaFolder,
            useFilenameVersions = true,
            flags = Flags.values().toList()
        )
    }
}

//INFO: set org.gradle.logging.level=info (e.g. gradle.properties) for log output
//TODO fix this: GitHub Runner complains about unknown definition
//  src-resolve: Cannot resolve the name 'articles:article' to a(n) 'type definition' component.
val generateJaxbTask = tasks.register<XsdJavaGeneratorTask>("generateJaxbTask") {
    outputDir = xjcGenDir
    schemas = fileTree(schemaFolder) { include("*.xsd") }.files
    bindings = schemas.map { File(it.parent, "${it.nameWithoutExtension}.xjb.xml") }.filter { it.exists() }
    episodes = emptyList()
    catalog = null
    createEpisode = false
    flags = Flags.values().toList()
    packageName = null
}

val generateJaxbAlternative = tasks.register("generateJaxbAlternative") {
    group = "generation"
    description = "Generates Java classes from XSD schemas"
    doLast {
        val generator = XsdJavaGenerator.create(xjcGenDir, encoding = Charsets.UTF_8, customLogger)
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

val generateJaxbSimple = tasks.register("generateJaxbSimple") {
    group = "generation"
    description = "Generates Java classes from XSD schemas"
    doLast {
        logger.lifecycle("Executing \"generateJaxbSimple\"")
        generate("complexParent_v6.xsd", "articleListCollection_v3.xsd")
    }
}

private fun generate(schema: String, vararg dependencies: String) {
    val generator = XsdJavaGenerator.create(xjcGenDir, encoding = Charsets.UTF_8, customLogger)
    generator.generateWithDependencies(
        schema,
        dependencies.toList(),
        schemaFolder = File(schemaFolder, "backup"),
        flags = Flags.values().toList()
    )
}

tasks.compileTestKotlin {
    dependsOn(generateJaxb)
}

tasks.test {
    useJUnitPlatform()

    jvmArgs(
        "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "--add-opens=java.base/java.lang.invoke=ALL-UNNAMED",
        "--add-opens=java.base/java.util=ALL-UNNAMED"
    )
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
                name.set("XSD Generator")
                description.set("Generates Java classes from XML Schema files (XSD) using Kotlin, JaxB and XJC")
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
