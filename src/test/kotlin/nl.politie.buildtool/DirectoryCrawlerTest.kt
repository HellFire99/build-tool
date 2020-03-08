package nl.politie.buildtool

import nl.politie.buildtool.utils.DirectoryCrawler
import org.junit.Test

class DirectoryCrawlerTest {

    lateinit var directoryCrawler: DirectoryCrawler
    @Test
    fun testGetPomFileList() {
        directoryCrawler = DirectoryCrawler()
        directoryCrawler.root = "."
        val fileList = directoryCrawler.getPomFileList()
        fileList.forEach { println("Project: ${it.name} - ${it.file.absoluteFile}") }
    }
}