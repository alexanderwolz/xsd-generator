package de.alexanderwolz.xsd.generator

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

class Example() {

    fun start() {
        val article1 = createArticleFromScratch()
        println(article1)
        val xml = createArticleXmlString(article1)
        println(xml)
        val article2 = createArticleFromXmlString(xml)
        println(article1)
        println(article2)
        println("equals: ${article1 == article2}")
    }

    private fun createArticleFromScratch(): Article {
        val article = ObjectFactory().createArticle()
        article.id = "1"
        article.author.add(Author().apply {
            id = "1"
            firstname = "John"
            lastname = "Doe"
        })
        article.publicationStatus = Status.DRAFT
        article.requiredRole = Role.EDITOR
        return article
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

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Example().start()
        }
    }

}