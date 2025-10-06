package de.alexanderwolz.xsd.generator

import de.alexanderwolz.commons.log.Logger
import org.slf4j.Logger as LoggerSLF4J
import java.io.File
import java.nio.charset.Charset

interface XsdJavaGenerator {

    companion object {

        fun create(outputDir: File, encoding: Charset = Charsets.UTF_8, logger: LoggerSLF4J? = null): XsdJavaGenerator {
            return XjcJavaGenerator(outputDir, encoding, logger)
        }

        fun create(outputDir: File, encoding: Charset = Charsets.UTF_8, logger: Logger): XsdJavaGenerator {
            return XjcJavaGenerator(outputDir, encoding, logger)
        }
    }

    fun generateAutoResolve(
        schemaFile: File,
        bindingFolder: File? = null,
        bindingExtension: String? = null,
        useFilenameVersions: Boolean = false,
        catalog: File? = null,
        flags: Collection<Flags>? = null,
        packageName: String? = null
    ): Boolean

    fun generateAutoResolve(
        schema: String,
        schemaFolder: File? = null,
        bindingFolder: File? = null,
        bindingExtension: String? = null,
        useFilenameVersions: Boolean = false,
        catalog: File? = null,
        flags: Collection<Flags>? = null,
        packageName: String? = null
    ): Boolean

    fun generate(
        schemas: Collection<File>,
        bindings: Collection<File>,
        episodes: Collection<File>,
        catalog: File?,
        createEpisode: Boolean,
        flags: Collection<Flags>?,
        packageName: String?
    ): Boolean
}