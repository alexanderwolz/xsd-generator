package de.alexanderwolz.xsd.generator

import com.sun.tools.xjc.Driver
import de.alexanderwolz.commons.log.Logger
import de.alexanderwolz.commons.util.version.VersionUtils
import de.alexanderwolz.commons.util.xsd.XsdFileReference
import de.alexanderwolz.commons.util.xsd.XsdReference
import de.alexanderwolz.commons.util.xsd.XsdUtils
import de.alexanderwolz.xsd.generator.exception.XsdCompileException
import de.alexanderwolz.xsd.generator.util.RecursionUtils
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.nio.charset.Charset

class XjcJavaGenerator(
    val outputDir: File, val encoding: Charset = Charsets.UTF_8, val logger: Logger
) : XsdJavaGenerator {

    constructor(
        outputDir: File, encoding: Charset = Charsets.UTF_8, logger: org.slf4j.Logger? = null
    ) : this(outputDir, encoding, logger?.let { Logger(it) } ?: Logger(XjcJavaGenerator::class))

    override fun generateAutoResolve(
        schema: String,
        schemaFolder: File?,
        bindingFolder: File?,
        bindingExtension: String,
        useFilenameVersions: Boolean,
        catalog: File?,
        flags: List<Flags>?,
        packageName: String?
    ): Boolean {

        val schemaFile = File(schemaFolder, schema)
        val allReferences = XsdUtils.getAllReferencedXsdSchemaFiles(schemaFile, schemaFolder)

        logger.info { "Generating schema ${schemaFile.name} with auto resolve .." }
        logger.trace { "Schema folder: $schemaFolder" }
        logger.trace { "Binding folder: $bindingFolder" }
        logger.trace { "Output folder: $outputDir" }
        logger.trace { "Filename Versions: $useFilenameVersions" }
        logger.trace { "Catalog: $catalog" }
        logger.trace { "flags: ${flags?.joinToString()}" }
        logger.trace { "packageName: $packageName" }

        val parent = allReferences.find { it.file == schemaFile }
        if (parent == null) {
            //should never happen
            throw NoSuchElementException("Could not find root element ($schemaFile)")
        }

        logTree(parent, logger)

        val bindingExt = bindingExtension.takeIf { it.startsWith(".") } ?: ".$bindingExtension"
        buildRecursive(parent, bindingFolder, bindingExt, packageName, useFilenameVersions, catalog, flags)
        return true
    }

    fun logTree(parent: XsdFileReference, logger: Logger) {
        logger.debug { "Found references:" }
        RecursionUtils.traverseTopDown(parent) { reference ->
            val depth = RecursionUtils.getDepth(reference)
            val indent = "  ".repeat(depth)
            logger.debug { "$indent${reference.schemaLocation} -> ${reference.file.parent}/${reference.file.name}" }
        }
    }

    fun buildRecursive(
        parent: XsdFileReference,
        bindingFolder: File?,
        bindingExtension: String,
        packageName: String?,
        useFilenameVersions: Boolean,
        catalog: File?,
        flags: Collection<Flags>?
    ) {
        logger.debug { "Building ${parent.schemaLocation}" }
        RecursionUtils.traverseBottomUp(parent) { reference ->
            if (reference.type == XsdReference.Type.INCLUDE) {
                logger.warn { "Ignoring ${reference.schemaLocation}, because it is included by ${reference.parent?.schemaLocation}" }
            } else {
                build(
                    reference,
                    bindingFolder,
                    bindingExtension,
                    packageName,
                    useFilenameVersions,
                    catalog,
                    flags
                )
            }
        }
    }

    private fun build(
        reference: XsdFileReference,
        bindingFolder: File?,
        bindingExtension: String,
        packageName: String?,
        useFilenameVersions: Boolean,
        catalog: File?,
        flags: Collection<Flags>?
    ) {
        logger.trace { "Building schema file ${reference.file.name} .." }

        var pkg = packageName
        if (useFilenameVersions) {
            val version = VersionUtils.getVersion(reference.file)
            XsdUtils.getTargetNamespace(reference.file.readText())?.let { namespace ->
                pkg = "${XsdUtils.getPackageName(namespace)}.${version.asString("_", "v")}"
            }
        }

        val episodes = getAllEpisodes(reference)
        val schemas = listOf(reference.file)
        val bindings = ArrayList<File>()

        val episode = File(outputDir, "${reference.file.nameWithoutExtension}.episode")
        if (episode.exists()) {
            //episodes.add(episode)
            logger.info { "Episode ${episode.name} exists, so we skip building this model (${reference.schemaLocation})" }
            return
        } else {
            val binding = getBinding(reference, bindingFolder, bindingExtension)
            if (binding.exists()) {
                bindings.add(binding)
            }
        }

        logger.trace { "Schemas: ${schemas.joinToString { it.name }}" }
        logger.trace { "Episodes: ${episodes.joinToString { it.name }}" }
        logger.trace { "Bindings: ${bindings.joinToString { it.name }}" }

        generate(schemas, emptyList(), episodes, catalog, true, flags, pkg)

    }

    private fun getAllEpisodes(reference: XsdFileReference): List<File> {
        val episodes = mutableSetOf<File>()
        collectEpisodesRecursive(reference, episodes, true)
        return episodes.toList()
    }

    private fun collectEpisodesRecursive(
        reference: XsdFileReference,
        episodes: MutableSet<File>,
        includeTransitive: Boolean = false
    ) {
        reference.children.forEach { child ->
            val isIncluded = child.type == XsdReference.Type.INCLUDE

            if (!isIncluded) {
                val episode = getEpisode(child)
                if (episode.exists() && isEpisodeValid(episode)) {
                    episodes.add(episode)
                }
            }

            if (isIncluded || includeTransitive) {
                collectEpisodesRecursive(child, episodes, includeTransitive = true)
            } else {
                collectEpisodesRecursive(child, episodes, includeTransitive = false)
            }
        }
    }

    private fun isEpisodeValid(episode: File): Boolean {
        if (episode.length() == 0L) return false
        val content = episode.readText()
        //it is not empty if it has at least two bindings elements
        return content.contains("<bindings")
                && content.indexOf("<bindings") != content.lastIndexOf("<bindings")
    }

    private fun getEpisode(reference: XsdFileReference): File {
        return File(outputDir, "${reference.file.nameWithoutExtension}.episode")
    }

    private fun getBinding(reference: XsdFileReference, bindingFolder: File?, bindingExtension: String): File {
        return File(bindingFolder, "${reference.file.nameWithoutExtension}$bindingExtension")
    }

    override fun generateWithDependencies(
        schema: String,
        dependencies: Collection<String>,
        schemaFolder: File?,
        bindingFolder: File?,
        bindingExtension: String,
        catalog: File?,
        createEpisode: Boolean,
        flags: List<Flags>?,
        packageName: String?
    ): Boolean {
        val schemas = listOf(File(schemaFolder, schema))
        val bindingExt = bindingExtension.takeIf { it.startsWith(".") } ?: ".$bindingExtension"
        val bindings =
            schemas.map { File(bindingFolder, "${it.nameWithoutExtension}$bindingExt") }.filter { it.exists() }
        val dependencyMap = HashMap<File, Collection<File>>()
        dependencies.forEach { dependency ->
            val depSchema = File(schemaFolder, dependency)
            val depBindings =
                listOf(File(bindingFolder, "${depSchema.nameWithoutExtension}$bindingExt")).filter { it.exists() }
            dependencyMap[depSchema] = depBindings
        }
        return generateWithDependencies(schemas, bindings, dependencyMap, catalog, createEpisode, flags, packageName)
    }


    override fun generateWithDependencies(
        schemas: List<File>,
        bindings: List<File>,
        dependencies: Map<File, Collection<File>>,
        catalog: File?,
        createEpisode: Boolean,
        flags: List<Flags>?,
        packageName: String?
    ): Boolean {
        logger.info { "Generating schemas (${schemas.joinToString { it.name }}) with dependencies (${dependencies.keys.joinToString { it.name }})" }

        val episodes = ArrayList<File>()
        dependencies.forEach { entry ->
            val dependency = entry.key
            val dependencyBindings = entry.value
            val episode = File(outputDir, "${dependency.nameWithoutExtension}.episode")
            if (!episode.exists()) {
                logger.info { "Episode for dependency ${dependency.name} does not exist, starting generation.." }
                generate(listOf(dependency), dependencyBindings, emptyList(), catalog, true, flags, packageName)
            }
            episodes.add(episode)
        }

        return generate(schemas, bindings, episodes, catalog, createEpisode, flags, packageName)
    }

    override fun generate(
        schemas: Collection<File>,
        bindings: Collection<File>,
        episodes: Collection<File>,
        catalog: File?,
        createEpisode: Boolean,
        flags: Collection<Flags>?,
        packageName: String?
    ): Boolean {

        logger.info { "Parsing schemas: ${schemas.joinToString { it.name }}" }

        if (schemas.isEmpty()) {
            throw NoSuchElementException("Schemas must be not be empty")
        }

        schemas.forEach {
            if (!it.exists()) {
                throw NoSuchElementException("Schema ${it.absolutePath} does not exist")
            }
        }

        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }

        val args = Arguments()
        args.add("-d", outputDir.absolutePath)
        args.add("-encoding", encoding.name())

        (flags ?: Flags.DEFAULTS).forEach {
            args.add(it.value, null)
        }

        packageName?.let {
            args.add("-p", it)
        }

        bindings.forEach { binding ->
            logger.debug { "Using binding: ${binding.absolutePath}" }
            if (binding.exists()) {
                args.add("-b", binding.absolutePath)
            } else throw NoSuchElementException("Binding $binding does not exist")
        }

        episodes.forEach { episode ->
            logger.debug { "Using episode: ${episode.absolutePath}" }
            if (episode.exists()) {
                //Info: we have to bind episode like bindings
                args.add("-b", episode.absolutePath)
            } else throw NoSuchElementException("Episode $episode does not exist")
        }

        if (createEpisode) {
            //Info: JXC writes only one episode file per run
            val schema = schemas.first()
            val episode = File(outputDir, "${schema.nameWithoutExtension}.episode")
            args.add("-episode", episode.absolutePath)
        }

        catalog?.let {
            if (it.exists()) {
                args.add("-catalog", it.absolutePath)
            } else throw NoSuchElementException("Catalog $it does not exist")
        }

        //last arguments are the schema files
        schemas.forEach {
            args.add(it.absolutePath)
        }

        logger.debug { "Executing args: ${args.getArgs().joinToString(separator = " ")}" }

        val statusStream = ByteArrayOutputStream()
        val errorStream = ByteArrayOutputStream()

        val exitCode = Driver.run(
            args.getArgs(), PrintStream(statusStream), PrintStream(errorStream)
        )

        val statusLines = parseStatus(statusStream.use { it.toString() })
        statusLines.forEach {
            logger.info { it }
        }

        val errors = parseErrors(errorStream.use { it.toString() })

        if (exitCode != 0) {
            throw XsdCompileException(exitCode, errors)
        }

        logger.info { "XJC generation successful." }
        return true
    }

    private fun parseStatus(statusText: String): List<String> {
        return statusText.split("\n").filter { it.isNotBlank() }
    }

    private fun parseErrors(errorText: String): List<String> {
        val errors = ArrayList<String>()
        val splits = errorText.split("\n").filter { it.isNotBlank() }
        if (splits.isNotEmpty()) {
            var builder = StringBuilder()
            splits.forEachIndexed { i, it ->
                if (it.startsWith("[ERROR] ")) {
                    if (i > 0) {
                        errors.add(builder.toString())
                    }
                    builder = StringBuilder(it.substring(7))
                } else {
                    builder.append("\n${it.trim()}")
                }
            }
            errors.add(builder.toString())
        }
        return errors
    }
}