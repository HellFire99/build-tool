package nl.politie.buildtool.utils


import nl.politie.buildtool.model.PomFile
import org.springframework.stereotype.Component
import org.springframework.util.ResourceUtils
import org.w3c.dom.Document
import org.w3c.dom.NodeList
import java.io.File
import javax.swing.ImageIcon
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

const val XPATH_ARTIFACT_ID = "/project/artifactId"
const val XPATH_VERSION = "/project/version"

fun createIcon(path: String) =
        ImageIcon(ResourceUtils.getFile("classpath:${path}").readBytes())

@Component
class DirectoryCrawler {

    fun getPomFileList(startDir: String): List<PomFile> {
        return File(startDir).walkTopDown()
                .filter { it.name == "pom.xml" }
                .map {
                    val pomXmlDoc = readXml(it)
                    PomFile(extractValue(pomXmlDoc, XPATH_ARTIFACT_ID),
                            extractValue(pomXmlDoc, XPATH_VERSION),
                            it)
                }.toList()
    }

    private fun extractValue(pomXmlDoc: Document, xpathString: String): String {
        val xpath = XPathFactory.newInstance().newXPath()
        val expr = xpath.compile(xpathString)
        val nodes = expr.evaluate(pomXmlDoc, XPathConstants.NODESET) as NodeList
        return if (nodes.item(0) != null) {
            nodes.item(0).textContent
        } else {
            ""
        }

    }

    fun readXml(xmlFile: File): Document {
        val xmlDoc: Document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlFile)
        xmlDoc.documentElement.normalize()
        return xmlDoc
    }
}