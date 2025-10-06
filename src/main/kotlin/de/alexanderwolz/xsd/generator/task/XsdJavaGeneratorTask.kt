package de.alexanderwolz.xsd.generator.task

import de.alexanderwolz.commons.log.Logger
import de.alexanderwolz.xsd.generator.Flags
import de.alexanderwolz.xsd.generator.XjcJavaGenerator
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
    @Optional
    var encoding: String = Charsets.UTF_8.name()

    @get:InputFiles
    lateinit var schemas: Collection<File>

    @get:Input
    @Optional
    var bindingExtension: String? = null

    @get:Input
    var useFilenameVersions: Boolean = false

    @get:InputFile
    @Optional
    var catalog: File? = null

    @get:Input
    @Optional
    var flags: Collection<Flags>? = null

    @get:Input
    @Optional
    var packageName: String? = null

    @TaskAction
    fun run() {
        val encoding = Charset.forName(encoding.uppercase())
        val customLogger = Logger(javaClass) {
            logger.lifecycle(it.message)
        }
        val generator = XsdJavaGenerator.create(outputDir, encoding, customLogger)
        schemas.forEach { schema ->
            logger.lifecycle("Generating $schema ..")
            generator.generateAutoResolve(
                schema,
                schema.parentFile,
                bindingExtension,
                useFilenameVersions,
                catalog,
                flags,
                packageName
            )
        }
    }
}