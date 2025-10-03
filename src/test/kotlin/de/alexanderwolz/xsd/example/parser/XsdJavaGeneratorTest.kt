package de.alexanderwolz.xsd.example.parser

import de.alexanderwolz.xsd.generator.XsdJavaGenerator
import de.alexanderwolz.xsd.generator.exception.XsdCompileException
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File

class XsdJavaGeneratorTest {

    private val schemaDir = File("schemas")
    private val outputParent = File("build/generated")
    private val outputDir = File(outputParent, "/sources/xjc/main/java")
    private val generator = XsdJavaGenerator(outputDir)

    private val defaultPackage = "generated"

    @BeforeEach
    fun before() {
        outputDir.deleteRecursively()
    }

    @AfterEach
    fun after() {
        outputParent.deleteRecursively()
    }

    private fun testIfExists(parent: File, fileNames: Collection<String>) {
        fileNames.forEach {
            val generatedFile = File(parent, it)
            assertTrue { generatedFile.exists() }
        }
    }

    @Test
    fun testGenerateSimple() {
        val schema = File(schemaDir, "order_v1.xsd")
        val bindings = emptyList<File>()
        val episodes = emptyList<File>()
        val catalog = null
        val createEpisode = false
        val flags = null
        val packageName = null
        generator.generate(listOf(schema), bindings, episodes, catalog, createEpisode, flags, packageName)
        testIfExists(outputDir, listOf("$defaultPackage/Order.java"))
    }

    @Test
    fun testGenerateSimpleCustomPackageName() {
        val schema = File(schemaDir, "order_v1.xsd")
        val bindings = emptyList<File>()
        val episodes = emptyList<File>()
        val catalog = null
        val createEpisode = false
        val flags = null
        val packageName = "com.test.xjc.generated"
        val packageFolder = packageName.replace(".", "/")
        generator.generate(listOf(schema), bindings, episodes, catalog, createEpisode, flags, packageName)
        testIfExists(outputDir, listOf("$packageFolder/Order.java"))
    }

    @Test
    fun testGenerateSimpleEpisodeCreation() {
        val schema = File(schemaDir, "order_v1.xsd")
        val bindings = emptyList<File>()
        val episodes = emptyList<File>()
        val catalog = null
        val createEpisode = true
        val flags = null
        val packageName = null
        generator.generate(listOf(schema), bindings, episodes, catalog, createEpisode, flags, packageName)
        testIfExists(outputDir, listOf("$defaultPackage/Order.java"))
        testIfExists(outputDir, listOf("order_v1.episode"))
    }

    @Test
    fun testGenerateSimpleCustomPackageNameAndEpisodeCreation() {
        val schema = File(schemaDir, "order_v1.xsd")
        val bindings = emptyList<File>()
        val episodes = emptyList<File>()
        val catalog = null
        val createEpisode = true
        val flags = null
        val packageName = "com.test.xjc.generated"
        val packageFolder = packageName.replace(".", "/")
        generator.generate(listOf(schema), bindings, episodes, catalog, createEpisode, flags, packageName)
        testIfExists(outputDir, listOf("$packageFolder/Order.java"))
        testIfExists(outputDir, listOf("order_v1.episode"))
        assertTrue {
            val episode = File(outputDir, "order_v1.episode").readText()
            episode.contains("<jaxb:package name=\"com.test.xjc.generated\"/>") && episode.contains("<jaxb:class ref=\"com.test.xjc.generated.Order\"/>")
        }
    }

    @Test
    fun testGenerateSimpleWithBinding() {
        val schema = File(schemaDir, "order_v1.xsd")
        val bindings = listOf(File(schemaDir, "${schema.nameWithoutExtension}.xjb.xml"))
        val episodes = emptyList<File>()
        val catalog = null
        val createEpisode = false
        val flags = null
        val packageName = null
        generator.generate(listOf(schema), bindings, episodes, catalog, createEpisode, flags, packageName)
        testIfExists(outputDir, listOf("de/alexanderwolz/generated/v1/Order.java"))
    }

    @Test
    fun testGenerateSimpleWithBindingAndEpisodeCreation() {
        val schema = File(schemaDir, "order_v1.xsd")
        val bindings = listOf(File(schemaDir, "${schema.nameWithoutExtension}.xjb.xml"))
        val episodes = emptyList<File>()
        val catalog = null
        val createEpisode = true
        val flags = null
        val packageName = null
        generator.generate(listOf(schema), bindings, episodes, catalog, createEpisode, flags, packageName)
        testIfExists(outputDir, listOf("de/alexanderwolz/generated/v1/Order.java"))
        testIfExists(outputDir, listOf("order_v1.episode"))
        assertTrue {
            val episode = File(outputDir, "order_v1.episode").readText()
            episode.contains("<jaxb:package name=\"de.alexanderwolz.generated.v1\"/>") && episode.contains("<jaxb:class ref=\"de.alexanderwolz.generated.v1.Order\"/>")
        }
    }

    @Test
    fun testGenerateNested() {
        val schema = File(schemaDir, "articleListCollection_v3.xsd")
        val bindings = emptyList<File>()
        val episodes = emptyList<File>()
        val catalog = null
        val createEpisode = false
        val flags = null
        val packageName = null
        generator.generate(listOf(schema), bindings, episodes, catalog, createEpisode, flags, packageName)
        //it should use the definition from target namespace, e.g. de.alexanderwolz.schema
        testIfExists(
            outputDir, listOf(
                "de/alexanderwolz/schema/articles/Article.java",
                "de/alexanderwolz/schema/articles/ArticleList.java",
                "de/alexanderwolz/schema/articles/ArticleListCollection.java",
                "de/alexanderwolz/schema/articles/Category.java",
                "de/alexanderwolz/schema/articles/Status.java",
                "de/alexanderwolz/schema/authors/Author.java",
                "de/alexanderwolz/schema/roles/Role.java",
            )
        )
    }

    @Test
    fun testGenerateNestedWithBinding() {
        val schema = File(schemaDir, "articleListCollection_v3.xsd")
        val bindings = listOf(File(schemaDir, "${schema.nameWithoutExtension}.xjb.xml"))
        val episodes = emptyList<File>()
        val catalog = null
        val createEpisode = false
        val flags = null
        val packageName = null
        generator.generate(listOf(schema), bindings, episodes, catalog, createEpisode, flags, packageName)
        testIfExists(
            outputDir, listOf(
                "de/alexanderwolz/model/article/v3/Article.java",
                "de/alexanderwolz/model/article/v3/ArticleList.java",
                "de/alexanderwolz/model/article/v3/ArticleListCollection.java",
                "de/alexanderwolz/model/article/v3/Category.java",
                "de/alexanderwolz/model/article/v3/Status.java",
                "de/alexanderwolz/model/author/v2/Author.java",
                "de/alexanderwolz/model/role/v6/Role.java",
            )
        )
    }

    @Test
    fun testGenerateNestedWithBindingAndEpisodeCreation() {
        val schema = File(schemaDir, "articleListCollection_v3.xsd")
        val bindings = listOf(File(schemaDir, "${schema.nameWithoutExtension}.xjb.xml"))
        val episodes = emptyList<File>()
        val catalog = null
        val createEpisode = true
        val flags = null
        val packageName = null
        generator.generate(listOf(schema), bindings, episodes, catalog, createEpisode, flags, packageName)
        testIfExists(
            outputDir, listOf(
                "de/alexanderwolz/model/article/v3/Article.java",
                "de/alexanderwolz/model/article/v3/ArticleList.java",
                "de/alexanderwolz/model/article/v3/ArticleListCollection.java",
                "de/alexanderwolz/model/article/v3/Category.java",
                "de/alexanderwolz/model/article/v3/Status.java",
                "de/alexanderwolz/model/author/v2/Author.java",
                "de/alexanderwolz/model/role/v6/Role.java",
            )
        )
        testIfExists(outputDir, listOf("articleListCollection_v3.episode"))
        assertTrue {
            val episode = File(outputDir, "articleListCollection_v3.episode").readText()
            episode.contains("<package name=\"de.alexanderwolz.model.article.v3\"/>")
                    && episode.contains("<class ref=\"de.alexanderwolz.model.article.v3.ArticleListCollection\"/>")
                    && episode.contains("<class ref=\"de.alexanderwolz.model.article.v3.ArticleList\"/>")
                    && episode.contains("<class ref=\"de.alexanderwolz.model.article.v3.Article\"/>")
                    && episode.contains("<typesafeEnumClass ref=\"de.alexanderwolz.model.article.v3.Status\"/>")
                    && episode.contains("<typesafeEnumClass ref=\"de.alexanderwolz.model.article.v3.Category\"/>")
                    && episode.contains("<class ref=\"de.alexanderwolz.model.author.v2.Author\"/>")
                    && episode.contains("<typesafeEnumClass ref=\"de.alexanderwolz.model.role.v6.Role\"/>")
        }
    }

    @Test
    fun testGenerateComplex() {
        val schema = File(schemaDir, "complexParent_v6.xsd")
        val bindings = emptyList<File>()
        val episodes = emptyList<File>()
        val catalog = null
        val createEpisode = false
        val flags = null
        val packageName = null
        generator.generate(listOf(schema), bindings, episodes, catalog, createEpisode, flags, packageName)
        //It generates all classes using the schemas defined in the XSDs
        testIfExists(
            outputDir, listOf(
                "de/alexanderwolz/schema/complex/Complex.java",
                "de/alexanderwolz/schema/articles/Article.java",
                "de/alexanderwolz/schema/articles/ArticleList.java",
                "de/alexanderwolz/schema/articles/Category.java",
                "de/alexanderwolz/schema/articles/Status.java",
                "de/alexanderwolz/schema/authors/Author.java",
                "de/alexanderwolz/schema/roles/Role.java"
            )
        )
    }

    @Test
    fun testGenerateComplexWithBinding() {
        val schema = File(schemaDir, "complexParent_v6.xsd")
        val bindings = listOf(File(schemaDir, "${schema.nameWithoutExtension}.xjb.xml"))
        val episodes = emptyList<File>()
        val catalog = null
        val createEpisode = false
        val flags = null
        val packageName = null
        generator.generate(listOf(schema), bindings, episodes, catalog, createEpisode, flags, packageName)
        //it only generates the package from the dedicated xsd binding, it takes the rest from the namespace
        testIfExists(
            outputDir, listOf(
                "de/alexanderwolz/model/complex/v6/Complex.java",
                "de/alexanderwolz/schema/articles/Article.java",
                "de/alexanderwolz/schema/articles/ArticleList.java",
                "de/alexanderwolz/schema/articles/Category.java",
                "de/alexanderwolz/schema/articles/Status.java",
                "de/alexanderwolz/schema/authors/Author.java",
                "de/alexanderwolz/schema/roles/Role.java"
            )
        )
    }

    @Test
    fun testGenerateComplexWithDependencyBindingFails() {
        val schema = File(schemaDir, "complexParent_v6.xsd")
        val bindings = listOf(
            File(schemaDir, "${schema.nameWithoutExtension}.xjb.xml"),
            File(schemaDir, "articleListCollection_v3.xjb.xml")
        )
        val episodes = emptyList<File>()
        val catalog = null
        val createEpisode = false
        val flags = null
        val packageName = null
        assertThrows<XsdCompileException> {
            //It must fail here, as the schema file from the 2nd binding is not specified
            generator.generate(listOf(schema), bindings, episodes, catalog, createEpisode, flags, packageName)
        }.printStackTrace()
    }

    @Test
    fun testGenerateComplexWithDependenciesAndBindings() {
        val schemas = listOf(
            File(schemaDir, "complexParent_v6.xsd"),
            File(schemaDir, "articleListCollection_v3.xsd")
        )
        val bindings = schemas.map { File(it.parent, "${it.nameWithoutExtension}.xjb.xml") }
        val episodes = emptyList<File>()
        val catalog = null
        val createEpisode = false
        val flags = null
        val packageName = null
        generator.generate(schemas, bindings, episodes, catalog, createEpisode, flags, packageName)
        //It uses all package names from both bindings
        testIfExists(
            outputDir, listOf(
                "de/alexanderwolz/model/complex/v6/Complex.java",
                "de/alexanderwolz/model/article/v3/Article.java",
                "de/alexanderwolz/model/article/v3/ArticleList.java",
                "de/alexanderwolz/model/article/v3/Category.java",
                "de/alexanderwolz/model/article/v3/Status.java",
                "de/alexanderwolz/model/author/v2/Author.java",
                "de/alexanderwolz/model/role/v6/Role.java"
            )
        )
    }

    @Test
    fun testGenerateComplexWithDependenciesAndBindingsAndCustomPackageName() {
        val schemas = listOf(
            File(schemaDir, "complexParent_v6.xsd"),
            File(schemaDir, "articleListCollection_v3.xsd")
        )
        val bindings = schemas.map { File(it.parent, "${it.nameWithoutExtension}.xjb.xml") }
        val episodes = emptyList<File>()
        val catalog = null
        val createEpisode = false
        val flags = null
        val packageName = "com.domain"
        generator.generate(schemas, bindings, episodes, catalog, createEpisode, flags, packageName)
        //It uses all package names from both bindings
        testIfExists(
            outputDir, listOf(
                "com/domain/Complex.java",
                "com/domain/Article.java",
                "com/domain/ArticleList.java",
                "com/domain/Category.java",
                "com/domain/Status.java",
                "com/domain/Author.java",
                "com/domain/Role.java"
            )
        )
    }

    @Test
    fun testGenerateComplexWithNonExistingEpisodesFails() {
        val schema = File(schemaDir, "complexParent_v6.xsd")
        val bindings = listOf(File(schemaDir, "${schema.nameWithoutExtension}.xjb.xml"))
        val episodes = listOf(File(outputDir, "articleListCollection_v3.episode"))
        val catalog = null
        val createEpisode = false
        val flags = null
        val packageName = null
        assertThrows<NoSuchElementException> {
            //It must fail here, as the episode does not exist
            generator.generate(listOf(schema), bindings, episodes, catalog, createEpisode, flags, packageName)
        }.printStackTrace()
    }

    @Test
    fun testGenerateComplexWithEpisodes() {

        //create the dependencies first
        testGenerateNestedWithBindingAndEpisodeCreation()

        val schema = File(schemaDir, "complexParent_v6.xsd")
        val bindings = listOf(File(schemaDir, "${schema.nameWithoutExtension}.xjb.xml"))
        val episodes = listOf(File(outputDir, "articleListCollection_v3.episode"))
        val catalog = null
        val createEpisode = false
        val flags = null
        val packageName = null
        generator.generate(listOf(schema), bindings, episodes, catalog, createEpisode, flags, packageName)
        //It should only create the complex class here
    }

    @Test
    fun testGenerateWithDependencies() {
        val schemas = listOf(File(schemaDir, "complexParent_v6.xsd"))
        val bindings = schemas.map { File(it.parent, "${it.nameWithoutExtension}.xjb.xml") }.filter { it.exists() }
        val dependencySchema = File(schemaDir, "articleListCollection_v3.xsd")
        val dependencyBinding = File(dependencySchema.parent, "${dependencySchema.nameWithoutExtension}.xjb.xml")
        val dependencies = mapOf(dependencySchema to listOf(dependencyBinding))
        val catalog = null
        val createEpisode = false
        val flags = null
        val packageName = null
        generator.generate(schemas, bindings, dependencies, catalog, createEpisode, flags, packageName)
    }


}