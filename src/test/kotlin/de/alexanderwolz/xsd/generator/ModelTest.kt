package de.alexanderwolz.xsd.generator

import de.alexanderwolz.commons.log.Logger
import de.alexanderwolz.model.article.v3.Article
import de.alexanderwolz.model.article.v3.ObjectFactory
import de.alexanderwolz.model.article.v3.Status
import de.alexanderwolz.model.author.v2.Author
import de.alexanderwolz.model.role.v6.Role
import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.JAXBElement
import java.io.StringWriter
import javax.xml.namespace.QName
import javax.xml.transform.stream.StreamSource
import kotlin.test.Test
import kotlin.test.assertEquals

class ModelTest() {

    private val logger = Logger(javaClass)

    @Test
    fun testModel() {
        val article2 = createArticleFromXmlString(articleXml)
        assertEquals(article, article2)
    }

    private fun createArticleFromXmlString(xml: String): Article {
        val context = JAXBContext.newInstance(Article::class.java)
        val element = context.createUnmarshaller().unmarshal(StreamSource(xml.byteInputStream()), Article::class.java)
        return element.value as Article
    }

    private fun createArticleXmlString(article: Article): String {
        val context = JAXBContext.newInstance(Article::class.java)
        val stringWriter = StringWriter()

        val qName = QName("http://alexanderwolz.de/model/article/v3", "article")
        val jaxbElement = JAXBElement(qName, Article::class.java, article)

        val marshaller = context.createMarshaller()
        marshaller.setProperty("jaxb.formatted.output", true) // Pretty print
        marshaller.marshal(jaxbElement, stringWriter)

        return stringWriter.toString()
    }

    private val articleXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
            "<ns3:article xmlns=\"http://www.alexanderwolz.de/schema/articles\" xmlns:ns2=\"http://www.alexanderwolz.de/schema/authors\" xmlns:ns3=\"http://alexanderwolz.de/model/article/v3\">\n" +
            "    <id>1</id>\n" +
            "    <ns2:author>\n" +
            "        <ns2:id>1</ns2:id>\n" +
            "        <ns2:firstname>John</ns2:firstname>\n" +
            "        <ns2:lastname>Doe</ns2:lastname>\n" +
            "    </ns2:author>\n" +
            "    <requiredRole>EDITOR</requiredRole>\n" +
            "    <publicationStatus>draft</publicationStatus>\n" +
            "</ns3:article>"

    private val article = ObjectFactory().createArticle().apply {
        id = "1"
        author.add(Author().apply {
            id = "1"
            firstname = "John"
            lastname = "Doe"
        })
        publicationStatus = Status.DRAFT
        requiredRole = Role.EDITOR
    }

}