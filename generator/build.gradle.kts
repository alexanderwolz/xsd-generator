plugins {
    id("java-library")
    kotlin("jvm")
    id("maven-publish")
    signing
}

base {
    archivesName.set("xsd-generator")
}

java {
    withSourcesJar()
    withJavadocJar()
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    api("org.slf4j:slf4j-api:2.0.17")
    api("org.glassfish.jaxb:jaxb-xjc:4.0.5")
    compileOnly(gradleApi())

    testImplementation(kotlin("test"))
    testImplementation("org.slf4j:slf4j-simple:2.0.17")
    testImplementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.2")
    testImplementation("org.glassfish.jaxb:jaxb-runtime:4.0.5")
    testImplementation("org.glassfish.jaxb:jaxb-xjc:4.0.5")
    testImplementation("org.jvnet.jaxb:jaxb-plugins:4.0.11")
}

tasks.test {
    useJUnitPlatform()
}

//see also https://github.com/gradle-nexus/publish-plugin/tree/v2.0.0
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "xsd-generator"
            pom {
                name.set("XSD Generator")
                description.set("Generates Java classes from XML Schema files (XSD) using Kotlin, JaxB and XJC")
                url.set("https://github.com/alexanderwolz/xsd-generator")
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
                    connection.set("scm:git:https://github.com/alexanderwolz/xsd-generator.git")
                    developerConnection.set("scm:git:ssh://git@github.com/alexanderwolz/xsd-generator.git")
                    url.set("https://github.com/alexanderwolz/xsd-generator")
                }
            }
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications["mavenJava"])
}
