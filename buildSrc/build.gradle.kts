import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("de.alexanderwolz:commons-log:1.1.0")
    implementation("de.alexanderwolz:commons-util:1.3.1")

    implementation("org.slf4j:slf4j-simple:2.0.17")
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.2")
    implementation("org.glassfish.jaxb:jaxb-runtime:4.0.6")
    implementation("org.glassfish.jaxb:jaxb-xjc:4.0.6")
    implementation("org.jvnet.jaxb:jaxb-plugins:4.0.11") //equals, toString, hashcode
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xskip-metadata-version-check")
    }
}