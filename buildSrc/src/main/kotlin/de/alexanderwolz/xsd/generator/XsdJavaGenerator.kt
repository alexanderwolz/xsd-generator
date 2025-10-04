package de.alexanderwolz.xsd.generator

import de.alexanderwolz.xsd.generator.instance.XjcXsdJavaGenerator
import org.slf4j.Logger
import java.io.File
import java.nio.charset.Charset

interface XsdJavaGenerator {

    companion object {
        fun create(outputDir: File, encoding: Charset = Charsets.UTF_8, logger: Logger? = null): XsdJavaGenerator {
            return XjcXsdJavaGenerator(outputDir, encoding, logger)
        }
    }

    fun generateAutoResolve(
        schema: String,
        schemaFolder: File?,
        bindingFolder: File? = schemaFolder,
        bindingExtension: String = ".xjb.xml",
        catalog: File? = null,
        createEpisode: Boolean = false,
        flags: List<Flags>? = null,
        packageName: String? = null
    ): Boolean

    fun generateWithDependencies(
        schema: String,
        dependencies: Collection<String> = emptyList(),
        schemaFolder: File? = null,
        bindingFolder: File? = schemaFolder,
        bindingExtension: String = ".xjb.xml",
        catalog: File? = null,
        createEpisode: Boolean = false,
        flags: List<Flags>? = null,
        packageName: String? = null
    ): Boolean


    fun generateWithDependencies(
        schemas: List<File>,
        bindings: List<File>,
        dependencies: Map<File, Collection<File>>,
        catalog: File? = null,
        createEpisode: Boolean = false,
        flags: List<Flags>? = null,
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