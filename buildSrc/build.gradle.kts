import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://repo1.maven.org/maven2")
}

dependencies {
    implementation("de.alexanderwolz:commons-util:1.4.6")
    implementation("org.glassfish.jaxb:jaxb-xjc:4.0.6")
    runtimeOnly("org.glassfish.jaxb:jaxb-runtime:4.0.6")
    runtimeOnly("org.jvnet.jaxb:jaxb-plugins:4.0.11") //equals, toString, hashcode
}

sourceSets {
    main {
        kotlin {
            srcDir("../src/main/kotlin")
        }
    }
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xskip-metadata-version-check")
    }
}