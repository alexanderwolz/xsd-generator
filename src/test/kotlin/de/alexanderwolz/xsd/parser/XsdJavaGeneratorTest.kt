package de.alexanderwolz.xsd.parser

import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertTrue

class XsdJavaGeneratorTest {

    private val schemaDir = File("src/test/resources/schema")
    private val outputDir = File("build/generated/sources/xjc/main/java")
    private val generator = XsdJavaGenerator(outputDir)

    @Test
    fun testGenerateSimpleXSD() {
        outputDir.deleteRecursively()
        val schema = File(schemaDir, "simple/order_v1.xsd")
        val bindings = listOf(File(schemaDir, "simple/${schema.nameWithoutExtension}.xjb.xml"))
        val episodes = emptyList<File>()
        val catalog = null
        val createEpisode = false
        val packageName = null
        generator.generate(schema, bindings, episodes, catalog, createEpisode, packageName)
        testIfExists(listOf("de/alexanderwolz/generated/v1/Order.java"))
    }

    @Test
    fun testGenerateComplexXSDs1() {
        outputDir.deleteRecursively()
        val schema = File(schemaDir, "complex/articleListCollection_v3.xsd")
        val bindings = listOf(File(schemaDir, "complex/binding/${schema.nameWithoutExtension}.xjb.xml"))
        val episodes = emptyList<File>()
        val catalog = null
        val createEpisode = false
        val packageName = null
        generator.generate(schema, bindings, episodes, catalog, createEpisode, packageName)
        testIfExists(
            listOf(
                "de/alexanderwolz/test/article/v3/Article.java",
                "de/alexanderwolz/test/article/v3/ArticleList.java",
                "de/alexanderwolz/test/article/v3/ArticleListCollection.java",
                "de/alexanderwolz/test/article/v3/Category.java",
                "de/alexanderwolz/test/article/v3/Status.java",
                "de/alexanderwolz/test/author/v2/Author.java",
                "de/alexanderwolz/test/role/v6/Role.java",
            )
        )
    }

    @Test
    fun testGenerateComplexXSDs2() {
        outputDir.deleteRecursively()
        val schema = File(schemaDir, "complex/complexParent_v6.xsd")
        val bindings = listOf(File(schemaDir, "complex/binding/${schema.nameWithoutExtension}.xjb.xml"))
        val episodes = emptyList<File>()
        val catalog = null
        val createEpisode = false
        val packageName = null
        generator.generate(schema, bindings, episodes, catalog, createEpisode, packageName)
        testIfExists(
            listOf(
                "de/alexanderwolz/test/complex/v6/Complex.java"
            )
        )
    }

    private fun testIfExists(fileNames: Collection<String>) {
        fileNames.forEach {
            val generatedFile = File(outputDir, it)
            assertTrue { generatedFile.exists() }
        }
    }

}