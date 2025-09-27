package de.alexanderwolz.xsd.parser

import com.sun.tools.xjc.Driver
import org.slf4j.LoggerFactory
import java.io.File


class XsdJavaGenerator(val outputDir: File) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun generate(
        schema: File,
        bindings: Collection<File>,
        episodes: Collection<File>,
        catalog: File?,
        createEpisode: Boolean,
        packageName: String?
    ) = generate(listOf(schema), bindings, episodes, catalog, createEpisode, packageName)


    fun generate(
        schemas: Collection<File>,
        bindings: Collection<File>,
        episodes: Collection<File>,
        catalog: File?,
        createEpisode: Boolean,
        packageName: String?
    ): Boolean {

        logger.info("Parsing schemas: ${schemas.joinToString { it.name }}")

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
        args.add("-encoding", "UTF-8")
        args.add("-extension")
        args.add("-XautoNameResolution")

        packageName?.let {
            args.add("-p", it)
        }

        bindings.forEach { binding ->
            if (binding.exists()) {
                args.add("-b", binding.absolutePath)
            } else throw NoSuchElementException("Binding $binding does not exist")
        }

        episodes.forEach { episode ->
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

        logger.info("Executing args: ${args.getArgs().joinToString(separator = " ")}")

        val statusStream = System.out
        val outStream = System.out
        val exitCode = Driver.run(args.getArgs(), statusStream, outStream)
        if (exitCode != 0) {
            throw Exception("XJC failed with exit code $exitCode")
        }

        logger.info("XJC generation successful.")
        return true
    }

    private class Arguments() {

        private val argsList = ArrayList<String>()

        fun add(value: String) {
            argsList.add(value)
        }

        fun add(key: String, value: String) {
            if (!key.startsWith("-")) throw IllegalArgumentException("Key must start with -")
            argsList.add(key)
            add(value)
        }

        fun getArgs(): Array<String> {
            return argsList.toTypedArray()
        }
    }

    private fun getPackageNameFromNamespace(schemaFile: File): String {
        val content = schemaFile.readText()
        val default = "generated"
        val regex = Regex("""targetNamespace\s*=\s*["'](.*?)["']""")
        regex.find(content)?.groupValues?.get(1)?.let { namespace ->
            val version = schemaFile.nameWithoutExtension.split("_", limit = 2)[1]
            return urlToPackage(namespace) + ".$version"
        }
        return default
    }

    private fun urlToPackage(url: String): String {
        val cleanedUrl = url.removePrefix("http://").removePrefix("https://").removePrefix("www.").trimEnd('/')
        val splits = cleanedUrl.split("/", limit = 2)
        val domainParts = splits[0].split(".").reversed()
        val pathParts = splits.getOrNull(1)?.split("/") ?: emptyList()
        return (domainParts + pathParts).filter { it.isNotBlank() }.joinToString(".").lowercase()
    }
}