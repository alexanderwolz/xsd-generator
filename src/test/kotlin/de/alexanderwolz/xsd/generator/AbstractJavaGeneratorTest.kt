package de.alexanderwolz.xsd.generator

import de.alexanderwolz.commons.log.Event
import de.alexanderwolz.commons.log.Logger
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.io.File

abstract class AbstractJavaGeneratorTest {

    protected val logEvents = ArrayList<Event>()
    protected val logger = Logger(javaClass) {
        println("[${it.level}] - ${it.message}")
        //println(it.message)
        logEvents.add(it)
    }

    protected val rootDir = File("").absoluteFile
    protected val schemaDir = File(rootDir, "schemas")
    protected val bindingsDir = File(schemaDir, "bindings")

    protected lateinit var generator: XsdJavaGenerator

    protected val defaultPackage = "generated"

    @TempDir
    protected lateinit var outputDir: File

    @BeforeEach
    fun before() {
        logger.debug { "Deleting output directory .." }
        outputDir.deleteRecursively()
        generator = XjcJavaGenerator(outputDir, logger = logger)
    }

    protected fun testIfExists(parent: File, fileNames: Collection<String>) {
        fileNames.forEach {
            val generatedFile = File(parent, it)
            assertTrue { generatedFile.exists() }
        }
    }


}