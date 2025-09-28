plugins {
    kotlin("jvm") version "2.2.10" apply false
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
}

allprojects {

    group = "de.alexanderwolz"
    version = "1.0.1"

    repositories {
        mavenCentral()
    }
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
        }
    }
}

subprojects {
    //exclude the example project
    if (name == "example") {
        tasks.withType<PublishToMavenRepository> {
            enabled = false
        }
    }
}