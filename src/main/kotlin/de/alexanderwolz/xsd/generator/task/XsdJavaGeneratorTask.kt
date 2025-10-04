package de.alexanderwolz.xsd.generator.task

import de.alexanderwolz.xsd.generator.XsdJavaGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.charset.Charset

open class XsdJavaGeneratorTask : DefaultTask() {

    @get:OutputDirectory
    lateinit var outputDir: File

    @get:Input
    var encoding: String = Charsets.UTF_8.name()

    @get:InputFiles
    lateinit var schemas: Collection<File>

    @get:InputFiles
    var bindings: Collection<File> = emptyList()

    @get:InputFiles
    var episodes: Collection<File> = emptyList()

    @get:InputFile
    @Optional
    var catalog: File? = null

    @get:Input
    var createEpisode: Boolean = false

    @get:Input
    @Optional
    var flags: Collection<XsdJavaGenerator.Flags>? = null

    @get:Input
    @Optional
    var packageName: String? = null

    @TaskAction
    fun run() {
        val encoding = Charset.forName(encoding.uppercase())
        val generator = XsdJavaGenerator(outputDir, encoding, logger)
        generator.generate(schemas, bindings, episodes, catalog, createEpisode, flags, packageName)
    }
}