package nl.politie.buildtool.maven

import nl.politie.buildtool.gui.BuildToolGUI
import nl.politie.buildtool.model.PomFile
import nl.politie.buildtool.model.PomFileTableModel
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import javax.swing.JCheckBox
import kotlin.concurrent.thread

@Component
class BuildExecutor(val buildToolMavenInvoker: BuildToolMavenInvoker) {
    private val logger = LoggerFactory.getLogger(BuildToolGUI::class.java)

    fun executeBuild(pomFileList: List<PomFile>,
                     pomTargetList: List<JCheckBox>,
                     tableModel: PomFileTableModel) {
        pomFileList.forEach { it.reset() }
        tableModel.fireTableDataChanged()

        val pomFiles = pomFileList
                .filter { it.checked }
        if (pomFiles.isEmpty()) {
            logger.info("Nothing to build. ")
            return
        }
        val targets = targets(pomTargetList)
        if (targets.isEmpty()) {
            logger.info("No targets selected. ")
            return
        }

        thread(start = true) {
            buildToolMavenInvoker.invoke(pomFiles, targets, tableModel)
        }
    }

    fun cancelBuild() {
        buildToolMavenInvoker.cancelBuild()
    }

    private fun targets(pomTargetList: List<JCheckBox>): List<String> {
        return pomTargetList.filter { it.isSelected }
                .map { it.name }
                .toList()
    }


}