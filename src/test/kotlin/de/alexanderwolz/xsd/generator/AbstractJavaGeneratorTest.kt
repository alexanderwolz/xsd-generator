package de.alexanderwolz.xsd.generator

import de.alexanderwolz.xsd.generator.instance.XjcXsdJavaGenerator
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertIs

abstract class AbstractJavaGeneratorTest {

    protected val schemaDir = File("schemas")
    protected lateinit var generator: XsdJavaGenerator

    protected val defaultPackage = "generated"

    @TempDir
    protected lateinit var outputDir: File

    @BeforeEach
    fun before() {
        generator = XsdJavaGenerator.create(outputDir)
        assertIs<XjcXsdJavaGenerator>(generator)
    }

    protected fun testIfExists(parent: File, fileNames: Collection<String>) {
        fileNames.forEach {
            val generatedFile = File(parent, it)
            assertTrue { generatedFile.exists() }
        }
    }


}