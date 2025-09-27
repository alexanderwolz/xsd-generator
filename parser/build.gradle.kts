

plugins {
    id("java-library")
    kotlin("jvm")
}

repositories {
    mavenCentral()
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

kotlin {
    jvmToolchain(22)
}