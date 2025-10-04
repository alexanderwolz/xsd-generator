plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "xsd-generator"


gradle.settingsEvaluated {
    val sourceDir = rootDir.resolve("src/main/kotlin")
    val buildSrcDir = rootDir.resolve("buildSrc/src/main/kotlin")
    logger.lifecycle("Syncing generator classes to buildSrc...")

    if (sourceDir.exists()) {
        buildSrcDir.deleteRecursively()
        sourceDir.copyRecursively(buildSrcDir, overwrite = true)
    }
}