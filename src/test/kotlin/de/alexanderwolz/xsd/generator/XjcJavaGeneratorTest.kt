package de.alexanderwolz.xsd.generator

import de.alexanderwolz.xsd.generator.exception.XsdCompileException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class XjcJavaGeneratorTest : AbstractJavaGeneratorTest() {

    @Test
    fun testSchemaFolder() {
        Assertions.assertTrue { schemaDir.exists() }
        Assertions.assertTrue { schemaDir.listFiles()?.isNotEmpty() ?: false }
        Assertions.assertTrue { bindingsDir.listFiles { it.name.endsWith("xjb.xml") }?.size == 3 }
        Assertions.assertTrue { schemaDir.listFiles { it.extension == "xsd" }?.size == 8 }
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
        Assertions.assertTrue {
            val episode = File(outputDir, "order_v1.episode").readText()
            episode.contains("<jaxb:package name=\"com.test.xjc.generated\"/>") && episode.contains("<jaxb:class ref=\"com.test.xjc.generated.Order\"/>")
        }
    }

    @Test
    fun testGenerateSimpleWithBinding() {
        val schema = File(schemaDir, "order_v1.xsd")
        val bindings = listOf(File(bindingsDir, "${schema.nameWithoutExtension}.xjb.xml"))
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
        val bindings = listOf(File(bindingsDir, "${schema.nameWithoutExtension}.xjb.xml"))
        val episodes = emptyList<File>()
        val catalog = null
        val createEpisode = true
        val flags = null
        val packageName = null
        generator.generate(listOf(schema), bindings, episodes, catalog, createEpisode, flags, packageName)
        testIfExists(outputDir, listOf("de/alexanderwolz/generated/v1/Order.java"))
        testIfExists(outputDir, listOf("order_v1.episode"))
        Assertions.assertTrue {
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
                "de/alexanderwolz/model/articles/Article.java",
                "de/alexanderwolz/model/articles/ArticleList.java",
                "de/alexanderwolz/model/articles/ArticleListCollection.java",
                "de/alexanderwolz/model/articles/Category.java",
                "de/alexanderwolz/model/articles/Status.java",
                "de/alexanderwolz/model/authors/Author.java",
                "de/alexanderwolz/model/roles/Role.java",
            )
        )
    }

    @Test
    fun testGenerateNestedWithBinding() {
        val schema = File(schemaDir, "articleListCollection_v3.xsd")
        val bindings = listOf(File(bindingsDir, "${schema.nameWithoutExtension}.xjb.xml"))
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
        val bindings = listOf(File(bindingsDir, "${schema.nameWithoutExtension}.xjb.xml"))
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
        Assertions.assertTrue {
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
                "de/alexanderwolz/model/complex/Complex.java",
                "de/alexanderwolz/model/articles/Article.java",
                "de/alexanderwolz/model/articles/ArticleList.java",
                "de/alexanderwolz/model/articles/Category.java",
                "de/alexanderwolz/model/articles/Status.java",
                "de/alexanderwolz/model/authors/Author.java",
                "de/alexanderwolz/model/roles/Role.java"
            )
        )
    }

    @Test
    fun testGenerateComplexWithBinding() {
        val schema = File(schemaDir, "complexParent_v6.xsd")
        val bindings = listOf(File(bindingsDir, "${schema.nameWithoutExtension}.xjb.xml"))
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
                "de/alexanderwolz/model/articles/Article.java",
                "de/alexanderwolz/model/articles/ArticleList.java",
                "de/alexanderwolz/model/articles/Category.java",
                "de/alexanderwolz/model/articles/Status.java",
                "de/alexanderwolz/model/authors/Author.java",
                "de/alexanderwolz/model/roles/Role.java"
            )
        )
    }

    @Test
    fun testGenerateComplexWithDependencyBindingFails() {
        val schema = File(schemaDir, "complexParent_v6.xsd")
        val bindings = listOf(
            File(bindingsDir, "${schema.nameWithoutExtension}.xjb.xml"),
            File(bindingsDir, "articleListCollection_v3.xjb.xml")
        )
        val episodes = emptyList<File>()
        val catalog = null
        val createEpisode = false
        val flags = null
        val packageName = null
        assertThrows<XsdCompileException> {
            //It must fail here, as the schema file from the 2nd binding is not specified
            generator.generate(listOf(schema), bindings, episodes, catalog, createEpisode, flags, packageName)
        }
    }

    @Test
    fun testGenerateComplexWithDependenciesAndBindings() {
        val schemas = listOf(
            File(schemaDir, "complexParent_v6.xsd"),
            File(schemaDir, "articleListCollection_v3.xsd")
        )
        val bindings = schemas.map { File(bindingsDir, "${it.nameWithoutExtension}.xjb.xml") }
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
        val bindings = schemas.map { File(bindingsDir, "${it.nameWithoutExtension}.xjb.xml") }
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
        val bindings = listOf(File(bindingsDir, "${schema.nameWithoutExtension}.xjb.xml"))
        val episodes = listOf(File(outputDir, "articleListCollection_v3.episode"))
        val catalog = null
        val createEpisode = false
        val flags = null
        val packageName = null
        assertThrows<NoSuchElementException> {
            //It must fail here, as the episode does not exist
            generator.generate(listOf(schema), bindings, episodes, catalog, createEpisode, flags, packageName)
        }
    }

    @Test
    fun testGenerateComplexWithEpisodes() {

        //create the dependencies first
        testGenerateNestedWithBindingAndEpisodeCreation()

        val schema = File(schemaDir, "complexParent_v6.xsd")
        val bindings = listOf(File(bindingsDir, "${schema.nameWithoutExtension}.xjb.xml"))
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
        val bindings = schemas.map { File(bindingsDir, "${it.nameWithoutExtension}.xjb.xml") }.filter { it.exists() }
        val dependencySchema = File(schemaDir, "articleListCollection_v3.xsd")
        val dependencyBinding = File(bindingsDir, "${dependencySchema.nameWithoutExtension}.xjb.xml")
        val dependencies = mapOf(dependencySchema to listOf(dependencyBinding))
        val catalog = null
        val createEpisode = false
        val flags = null
        val packageName = null
        generator.generateWithDependencies(schemas, bindings, dependencies, catalog, createEpisode, flags, packageName)
    }


    @Test
    fun testGenerateStringReferences() {
        generator.generateWithDependencies(
            "complexParent_v6.xsd",
            listOf("articleListCollection_v3.xsd"),
            schemaDir,
            bindingsDir
        )
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
    fun testAutoResolveWithVersionFromFilename() {

        logEvents.clear()
        generator.generateAutoResolve("status_v1.xsd", schemaDir, useFilenameVersions = true).apply {
            //only generates model: Status
            assertTrue { File(outputDir, "status_v1.episode").exists() }
            val logs = logEvents.map { it.message }.filter { it.startsWith("de/alexanderwolz") }
            assertEquals(2, logs.size)
            assertEquals("de/alexanderwolz/model/articles/v1/ObjectFactory.java", logs[0])
            assertEquals("de/alexanderwolz/model/articles/v1/Status.java", logs[1])
        }

        logEvents.clear()
        generator.generateAutoResolve("author_v2.xsd", schemaDir, useFilenameVersions = true).apply {
            //only generates model: Author
            assertTrue { File(outputDir, "status_v1.episode").exists() }
            assertTrue { File(outputDir, "author_v2.episode").exists() }
            val logs = logEvents.map { it.message }.filter { it.startsWith("de/alexanderwolz") }
            assertEquals(3, logs.size)
            assertEquals("de/alexanderwolz/model/authors/v2/ObjectFactory.java", logs[1])
            assertEquals("de/alexanderwolz/model/authors/v2/Author.java", logs[0])
            assertEquals("de/alexanderwolz/model/authors/v2/package-info.java", logs[2])
        }

        logEvents.clear()
        generator.generateAutoResolve("article_v3.xsd", schemaDir, useFilenameVersions = true).apply {
            //only generates model: Status, Author, Role, Article -> should only generate Article and Role
            val logs = logEvents.map { it.message }.filter { it.startsWith("de/alexanderwolz") }.sorted().onEach { println(it) }
            assertEquals(8, logs.size)
            assertEquals("de/alexanderwolz/model/articles/v3/Article.java", logs[0])
            assertEquals("de/alexanderwolz/model/articles/v3/ArticleList.java", logs[1])
            assertEquals("de/alexanderwolz/model/articles/v3/Category.java", logs[2])
            assertEquals("de/alexanderwolz/model/articles/v3/ObjectFactory.java", logs[3])
            assertEquals("de/alexanderwolz/model/articles/v3/Status.java", logs[4])
            assertEquals("de/alexanderwolz/model/articles/v3/package-info.java", logs[5])
            assertEquals("de/alexanderwolz/model/roles/v6/ObjectFactory.java", logs[6])
            assertEquals("de/alexanderwolz/model/roles/v6/Role.java", logs[7])
        }

        logEvents.clear()
        listOf(Flags.EXTENSION, Flags.GENERATE_EQUALS, Flags.GENERATE_TO_STRING, Flags.GENERATE_HASH_CODE)
        generator.generateAutoResolve("complexParent_v6.xsd", schemaDir, useFilenameVersions = true).apply {
            //only generates complex article
            val logs =
                logEvents.map { it.message }.filter { it.startsWith("de/alexanderwolz") }.onEach { println(it) }
            assertEquals(3, logs.size)
            assertEquals("de/alexanderwolz/model/complex/v6/Complex.java", logs[0])
            assertEquals("de/alexanderwolz/model/complex/v6/ObjectFactory.java", logs[1])
            assertEquals("de/alexanderwolz/model/complex/v6/package-info.java", logs[2])
        }
    }

    @Test
    fun testAutoResolveComplexParentWithFileNameVersion() {
        logEvents.clear()
        listOf(Flags.EXTENSION, Flags.GENERATE_EQUALS, Flags.GENERATE_TO_STRING, Flags.GENERATE_HASH_CODE)
        generator.generateAutoResolve("complexParent_v6.xsd", schemaDir, useFilenameVersions = true).apply {
            //only generates complex article
            val logs = logEvents.map { it.message }.filter { it.startsWith("de/alexanderwolz") }.sorted()
            assertEquals(14, logs.size)
            assertEquals("de/alexanderwolz/model/articles/v3/Article.java", logs[0])
            assertEquals("de/alexanderwolz/model/articles/v3/ArticleList.java", logs[1])
            assertEquals("de/alexanderwolz/model/articles/v3/Category.java", logs[2])
            assertEquals("de/alexanderwolz/model/articles/v3/ObjectFactory.java", logs[3])
            assertEquals("de/alexanderwolz/model/articles/v3/Status.java", logs[4])
            assertEquals("de/alexanderwolz/model/articles/v3/package-info.java", logs[5])
            assertEquals("de/alexanderwolz/model/authors/v2/Author.java", logs[6])
            assertEquals("de/alexanderwolz/model/authors/v2/ObjectFactory.java", logs[7])
            assertEquals("de/alexanderwolz/model/authors/v2/package-info.java", logs[8])
            assertEquals("de/alexanderwolz/model/complex/v6/Complex.java", logs[9])
            assertEquals("de/alexanderwolz/model/complex/v6/ObjectFactory.java", logs[10])
            assertEquals("de/alexanderwolz/model/complex/v6/package-info.java", logs[11])
            assertEquals("de/alexanderwolz/model/roles/v6/ObjectFactory.java", logs[12])
            assertEquals("de/alexanderwolz/model/roles/v6/Role.java", logs[13])
        }
    }

    @Test
    fun testAutoResolveComplexParentNamespace() {
        logEvents.clear()
        listOf(Flags.EXTENSION, Flags.GENERATE_EQUALS, Flags.GENERATE_TO_STRING, Flags.GENERATE_HASH_CODE)
        generator.generateAutoResolve("complexParent_v6.xsd", schemaDir).apply {
            //only generates complex article
            val logs = logEvents.map { it.message }.filter { it.startsWith("de/alexanderwolz") }.sorted()
            assertEquals(14, logs.size)
            assertEquals("de/alexanderwolz/model/articles/Article.java", logs[0])
            assertEquals("de/alexanderwolz/model/articles/ArticleList.java", logs[1])
            assertEquals("de/alexanderwolz/model/articles/Category.java", logs[2])
            assertEquals("de/alexanderwolz/model/articles/ObjectFactory.java", logs[3])
            assertEquals("de/alexanderwolz/model/articles/Status.java", logs[4])
            assertEquals("de/alexanderwolz/model/articles/package-info.java", logs[5])
            assertEquals("de/alexanderwolz/model/authors/Author.java", logs[6])
            assertEquals("de/alexanderwolz/model/authors/ObjectFactory.java", logs[7])
            assertEquals("de/alexanderwolz/model/authors/package-info.java", logs[8])
            assertEquals("de/alexanderwolz/model/complex/Complex.java", logs[9])
            assertEquals("de/alexanderwolz/model/complex/ObjectFactory.java", logs[10])
            assertEquals("de/alexanderwolz/model/complex/package-info.java", logs[11])
            assertEquals("de/alexanderwolz/model/roles/ObjectFactory.java", logs[12])
            assertEquals("de/alexanderwolz/model/roles/Role.java", logs[13])
        }
    }

    @Test
    fun testAutoResolveComplexParentCustomPackageName() {
        logEvents.clear()
        listOf(Flags.EXTENSION, Flags.GENERATE_EQUALS, Flags.GENERATE_TO_STRING, Flags.GENERATE_HASH_CODE)
        val packageName = "generated.model"
        generator.generateAutoResolve("complexParent_v6.xsd", schemaDir, packageName = packageName).apply {
            //only generates complex article
            val logs = logEvents.map { it.message }.filter { it.startsWith("generated/") }.distinct().sorted()
            assertEquals(9, logs.size)
            assertEquals("generated/model/Article.java", logs[0])
            assertEquals("generated/model/ArticleList.java", logs[1])
            assertEquals("generated/model/Author.java", logs[2])
            assertEquals("generated/model/Category.java", logs[3])
            assertEquals("generated/model/Complex.java", logs[4])
            assertEquals("generated/model/ObjectFactory.java", logs[5])
            assertEquals("generated/model/Role.java", logs[6])
            assertEquals("generated/model/Status.java", logs[7])
            assertEquals("generated/model/package-info.java", logs[8])
        }
    }

}