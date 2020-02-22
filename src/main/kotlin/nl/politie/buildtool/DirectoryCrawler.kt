package nl.politie.buildtool


import nl.politie.buildtool.model.PomFile
import org.springframework.stereotype.Component
import org.w3c.dom.Document
import org.w3c.dom.NodeList
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory


@Component
class DirectoryCrawler {

    fun getPomFileList(startDir: String): List<PomFile> {
        return File(startDir).walkTopDown()
                .filter { it.name == "pom.xml" }
                .map { PomFile(it, extractName(it)) }.toList()
    }

    private fun extractName(it: File): String {
        val pomXmlDoc = readXml(it)
        val xpath = XPathFactory.newInstance().newXPath()
        val expr = xpath.compile("/project/artifactId")
        val nodes = expr.evaluate(pomXmlDoc, XPathConstants.NODESET) as NodeList
        return nodes.item(0).textContent
    }

    fun readXml(xmlFile: File): Document {
        val xmlDoc: Document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlFile)
        xmlDoc.documentElement.normalize()
        return xmlDoc
    }
}