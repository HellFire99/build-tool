package nl.politie.buildtool

import org.junit.Test

class DirectoryCrawlerTest {

    lateinit var directoryCrawler: DirectoryCrawler
    @Test
    fun testGetPomFileList() {
        directoryCrawler = DirectoryCrawler()
        val fileList = directoryCrawler.getPomFileList("D:/Java/projects")
        fileList.forEach { println("Project: ${it.name} - ${it.file.absoluteFile}") }
    }
}