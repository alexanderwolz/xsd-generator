package de.alexanderwolz.xsd.generator

import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class XsdJavaGeneratorTest : AbstractJavaGeneratorTest() {

    @Test
    fun testCreate() {
        XsdJavaGenerator.create(outputDir, Charsets.ISO_8859_1, logger).apply {
            assertNotNull(this)
            assertIs<XjcJavaGenerator>(this)
        }
        XsdJavaGenerator.create(outputDir, Charsets.UTF_8, LoggerFactory.getLogger("SLF4J")).apply {
            assertNotNull(this)
            assertIs<XjcJavaGenerator>(this)
        }
    }
}