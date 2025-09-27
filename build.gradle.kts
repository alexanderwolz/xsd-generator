plugins {
    kotlin("jvm") version "2.2.10" apply false
}

allprojects {

    group = "de.alexanderwolz.xsd"
    version = "1.0.0" //should fit git tag for release

    repositories {
        mavenCentral()
    }
}