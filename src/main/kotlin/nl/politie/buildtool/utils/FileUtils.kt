package nl.politie.buildtool.utils

import javax.swing.ImageIcon

object FileUtils {
    fun createIcon(path: String) =
            ImageIcon(javaClass.getResource(path).readBytes())
}