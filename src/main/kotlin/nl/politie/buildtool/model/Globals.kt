package nl.politie.buildtool.model

import javax.swing.DefaultListModel
import javax.swing.JCheckBox

object Globals {
    // A list of all the pom files
    var pomFileList = listOf<PomFile>()

    // List of selected pom files
    val selectedPomNamesListModel = DefaultListModel<String>()

    // Maven target list
    val mavenTargetList = mutableListOf<JCheckBox>()

    // Map with all checkboxes
    var checkboxMap = mutableMapOf<String, JCheckBox>()

    fun isStopOnError() = checkboxMap[TXT_STOP_ON_ERROR]?.isSelected ?: false

}