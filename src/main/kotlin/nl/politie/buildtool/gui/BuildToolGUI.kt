package nl.politie.buildtool.gui

import nl.politie.buildtool.maven.BuildToolMavenInvoker
import nl.politie.buildtool.model.Column
import nl.politie.buildtool.model.PomFile
import nl.politie.buildtool.model.PomFileTableModel
import nl.politie.buildtool.utils.DirectoryCrawler
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.event.ActionEvent
import javax.swing.*
import javax.swing.GroupLayout.Alignment
import javax.swing.LayoutStyle.ComponentPlacement
import kotlin.concurrent.thread


@Component
class BuildToolGUI(val directoryCrawler: DirectoryCrawler,
                   val buildToolMavenInvoker: BuildToolMavenInvoker) {
    private val logger = LoggerFactory.getLogger(BuildToolGUI::class.java)
    lateinit var frmBuildtoolui: JFrame
    lateinit var tableModel: PomFileTableModel

    val pomTargetList = listOf(
            jCheckBox("clean", true),
            jCheckBox("compile", false),
            jCheckBox("install", true)
    )
    var pomFileList = listOf<PomFile>()
    var pomFileCheckBoxes = mutableListOf<JCheckBox>()

    /**
     * Initialize the contents of the frame.
     */
    fun initialize() {
        initJFrame()
        val pomPanel = JPanel()
        val mavenOptionsPanel = mavenOptionsPanel()
        val panel = JPanel()
        buildButton(panel)

        val glPomPanel = pomsPanel(pomPanel)

        pomPanel.layout = glPomPanel
        frmBuildtoolui.contentPane.layout = groupLayout(pomPanel, mavenOptionsPanel, panel)
    }

    private fun initJFrame() {
        frmBuildtoolui = JFrame()
        frmBuildtoolui.foreground = Color.LIGHT_GRAY
        frmBuildtoolui.title = "BuildToolUI"
        frmBuildtoolui.setBounds(100, 100, 721, 577)
        frmBuildtoolui.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    }

    private fun pomsPanel(pomPanel: JPanel): GroupLayout {
        val lbPoms = JLabel("pom's")
        lbPoms.font = Font("Arial", Font.PLAIN, 16)

        val pomsScrollPane = JScrollPane()
        pomsScrollPane.viewportBorder = null
        val glPomPanel = GroupLayout(pomPanel)
        glPomPanel.setHorizontalGroup(
                glPomPanel.createParallelGroup(Alignment.LEADING)
                        .addGroup(glPomPanel.createSequentialGroup()
                                .addComponent(lbPoms, GroupLayout.PREFERRED_SIZE, 570, GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(417, Short.MAX_VALUE.toInt()))
                        .addComponent(pomsScrollPane, GroupLayout.DEFAULT_SIZE, 570, Short.MAX_VALUE.toInt())
        )
        glPomPanel.setVerticalGroup(
                glPomPanel.createParallelGroup(Alignment.TRAILING)
                        .addGroup(Alignment.LEADING, glPomPanel.createSequentialGroup()
                                .addComponent(lbPoms)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(pomsScrollPane, GroupLayout.DEFAULT_SIZE, 453, Short.MAX_VALUE.toInt()))
        )
        val pomsContentPanel = JPanel()
        pomsScrollPane.setColumnHeaderView(pomsContentPanel)
        pomsContentPanel.layout = BoxLayout(pomsContentPanel, BoxLayout.Y_AXIS)

        // list of pom projects
        pomCheckBoxes(pomsContentPanel)

        return glPomPanel
    }

    private fun mavenOptionsPanel(): JPanel {
        val mavenOptionsPanel = JPanel()

        val lblMavenTargets = JLabel("Targets")
        lblMavenTargets.font = Font("Arial", Font.PLAIN, 16)
        val targetsScrollPane = JScrollPane()

        val glMavenOptionsPanel = GroupLayout(mavenOptionsPanel)
        glMavenOptionsPanel.setHorizontalGroup(
                glMavenOptionsPanel.createParallelGroup(Alignment.LEADING)
                        .addGroup(glMavenOptionsPanel.createSequentialGroup()
                                .addComponent(lblMavenTargets, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
                        .addComponent(targetsScrollPane, GroupLayout.DEFAULT_SIZE, 100, 100)
        )
        glMavenOptionsPanel.setVerticalGroup(
                glMavenOptionsPanel.createParallelGroup(Alignment.LEADING)
                        .addGroup(glMavenOptionsPanel.createSequentialGroup()
                                .addComponent(lblMavenTargets, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(targetsScrollPane, GroupLayout.DEFAULT_SIZE, 453, Short.MAX_VALUE.toInt()))
        )
        val targetsContentPanel = JPanel()
        targetsScrollPane.setViewportView(targetsContentPanel)
        targetsContentPanel.layout = BoxLayout(targetsContentPanel, BoxLayout.Y_AXIS)

        // Pom target checkboxes
        pomTargetCheckBoxes(targetsContentPanel)

        mavenOptionsPanel.layout = glMavenOptionsPanel
        return mavenOptionsPanel
    }

    private fun groupLayout(pomPanel: JPanel, mavenOptionsPanel: JPanel, panel: JPanel): GroupLayout {
        val groupLayout = GroupLayout(frmBuildtoolui.contentPane)
        groupLayout.setHorizontalGroup(
                groupLayout.createParallelGroup(Alignment.LEADING)
                        .addGroup(groupLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                                        .addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
                                                .addComponent(pomPanel, GroupLayout.DEFAULT_SIZE, 460, Short.MAX_VALUE.toInt())
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addComponent(mavenOptionsPanel, GroupLayout.PREFERRED_SIZE, 207, GroupLayout.PREFERRED_SIZE))
                                        .addComponent(panel, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 673, Short.MAX_VALUE.toInt()))
                                .addContainerGap())
        )
        groupLayout.setVerticalGroup(
                groupLayout.createParallelGroup(Alignment.LEADING)
                        .addGroup(groupLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
                                        .addComponent(mavenOptionsPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
                                        .addComponent(pomPanel, GroupLayout.PREFERRED_SIZE, 471, Short.MAX_VALUE.toInt()))
                                .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
                                .addComponent(panel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
        )
        return groupLayout
    }

    private fun pomCheckBoxes(pomsContentPanel: JPanel) {
        pomFileList = directoryCrawler.getPomFileList("..")
        tableModel = PomFileTableModel(pomFileList)
        val table = JTable(tableModel)
        val scrollPane = JScrollPane(table)
        scrollPane.preferredSize = Dimension(380, 450)

        table.columnModel.getColumn(0).preferredWidth = Column.CHECKED.width
        table.columnModel.getColumn(1).preferredWidth = Column.NAME.width
        table.columnModel.getColumn(2).preferredWidth = Column.VERSION.width
        table.columnModel.getColumn(3).preferredWidth = Column.START.width
        table.columnModel.getColumn(4).preferredWidth = Column.FINISHED.width
        table.columnModel.getColumn(5).preferredWidth = Column.DURATION.width
        table.columnModel.getColumn(6).preferredWidth = Column.STATUS.width

        pomsContentPanel.add(scrollPane, BorderLayout.CENTER)

    }

    private fun pomTargetCheckBoxes(targetsContentPanel: JPanel) {
        pomTargetList.forEach { targetsContentPanel.add(it) }
    }

    private fun jCheckBox(text: String, checked: Boolean = false): JCheckBox {
        val chckbxClean = JCheckBox(text)
        chckbxClean.name = text
        chckbxClean.isSelected = checked
        return chckbxClean
    }

    private fun buildButton(panel: JPanel) {
        val btnBuild = JButton("Build")
        btnBuild.addActionListener { executeBuild(it) }
        btnBuild.font = Font("Arial", Font.PLAIN, 14)
        panel.add(btnBuild)
    }

    private fun executeBuild(actionEvent: ActionEvent?) {
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

    private fun targets(pomTargetList: List<JCheckBox>): List<String> {
        return pomTargetList.filter { it.isSelected }
                .map { it.name }
                .toList()
    }

    fun setVisible(visible: Boolean) {
        frmBuildtoolui.isVisible = true

    }
}