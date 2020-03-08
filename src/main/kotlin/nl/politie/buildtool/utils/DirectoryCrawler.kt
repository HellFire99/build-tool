package nl.politie.buildtool.utils


import nl.politie.buildtool.maven.BuildToolMavenInvoker
import nl.politie.buildtool.model.PomFile
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.w3c.dom.Document
import org.w3c.dom.NodeList
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

const val XPATH_ARTIFACT_ID = "/project/artifactId"
const val XPATH_VERSION = "/project/version"

@Component
class DirectoryCrawler {
    private val logger = LoggerFactory.getLogger(BuildToolMavenInvoker::class.java)
    private val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()

    fun getPomFileList(startDir: String): List<PomFile> {
        logger.info("Searching pom files in $startDir")
        val pomFilesFound = File(startDir).walkTopDown()
                .maxDepth(3)
                .filter { it.name == "pom.xml" }
                .map {
                    val pomXmlDoc = readXml(it)

                    PomFile(extractValue(pomXmlDoc, XPATH_ARTIFACT_ID),
                            extractValue(pomXmlDoc, XPATH_VERSION),
                            it)
                }.toList()
                .sortedBy { it.name }
        pomFilesFound.forEach { logger.info(it.toString()) }
        logger.info("${pomFilesFound.size} Pom files found. ")
        return pomFilesFound
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
        val xmlDoc: Document = documentBuilder.parse(xmlFile)
        xmlDoc.documentElement.normalize()
        return xmlDoc
    }
}