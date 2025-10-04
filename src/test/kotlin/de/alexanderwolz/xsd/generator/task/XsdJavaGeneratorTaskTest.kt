package de.alexanderwolz.xsd.generator.task

import de.alexanderwolz.xsd.generator.AbstractJavaGeneratorTest
import de.alexanderwolz.xsd.generator.Flags
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals

@Suppress("DEPRECATION")
class XsdJavaGeneratorTaskTest : AbstractJavaGeneratorTest() {

    @Test
    fun testTask() {
        val project = ProjectBuilder.builder().build()
        val task = project.tasks.create("testXsdJavaGenerator", XsdJavaGeneratorTask::class.java)

        val schema = File(schemaDir, "articleListCollection_v3.xsd")

        // Configure the task
        task.outputDir = outputDir
        task.encoding = Charsets.UTF_8.name()
        task.schemas = listOf(schema)
        task.bindings = emptyList()
        task.episodes = emptyList()
        task.catalog = null
        task.createEpisode = false
        task.flags = Flags.entries
        task.packageName = null

        // Execute the task action
        task.actions.forEach { action ->
            action.execute(task)
        }

        val generatedFiles = outputDir.walk().filter {
            it.isFile
                    && it.extension == "java"
                    && it.nameWithoutExtension != "ObjectFactory"
                    && it.nameWithoutExtension != "package-info"
        }.toList()

        val expectedFiles = listOf(
            "de/alexanderwolz/schema/articles/Article.java",
            "de/alexanderwolz/schema/articles/ArticleList.java",
            "de/alexanderwolz/schema/articles/ArticleListCollection.java",
            "de/alexanderwolz/schema/articles/Category.java",
            "de/alexanderwolz/schema/articles/Status.java",
            "de/alexanderwolz/schema/authors/Author.java",
            "de/alexanderwolz/schema/roles/Role.java",
        )
        assertEquals(expectedFiles.size, generatedFiles.size)
        testIfExists(outputDir, expectedFiles)
    }
}