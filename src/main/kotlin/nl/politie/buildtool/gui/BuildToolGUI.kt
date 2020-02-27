package nl.politie.buildtool.gui

import nl.politie.buildtool.maven.BuildToolMavenInvoker
import nl.politie.buildtool.model.Column
import nl.politie.buildtool.model.PomFile
import nl.politie.buildtool.model.PomFileTableModel
import nl.politie.buildtool.model.SelectedProjectsListModel
import nl.politie.buildtool.utils.DirectoryCrawler
import nl.politie.buildtool.utils.createIcon
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.awt.Color
import java.awt.FlowLayout
import java.awt.Font
import java.awt.Insets
import java.awt.event.ActionEvent
import java.io.IOException
import javax.swing.*
import javax.swing.GroupLayout.Alignment
import javax.swing.LayoutStyle.ComponentPlacement
import javax.swing.border.BevelBorder
import javax.swing.border.EtchedBorder
import kotlin.concurrent.thread


@Component
class BuildToolGUI(val directoryCrawler: DirectoryCrawler,
                   val buildToolMavenInvoker: BuildToolMavenInvoker) {

    private val logger = LoggerFactory.getLogger(BuildToolGUI::class.java)
    lateinit var frmBuildtoolui: JFrame
    lateinit var tableModel: PomFileTableModel
    private var table: JTable? = null
    private var lbStatus = JLabel("Pom's")

    private var pomFileList = listOf<PomFile>()
    private var pomFileCheckBoxes = mutableListOf<JCheckBox>()
    private val pomTargetList: List<JCheckBox> = mutableListOf()

    init {
        initTable()
    }

    private fun initTable() {
        pomFileList = directoryCrawler.getPomFileList("..")
        tableModel = PomFileTableModel(pomFileList)
        val myTable = JTable(tableModel)
        myTable.columnModel.getColumn(0).preferredWidth = Column.CHECKED.width
        myTable.columnModel.getColumn(1).preferredWidth = Column.NAME.width
        myTable.columnModel.getColumn(2).preferredWidth = Column.VERSION.width
        myTable.columnModel.getColumn(3).preferredWidth = Column.START.width
        myTable.columnModel.getColumn(4).preferredWidth = Column.FINISHED.width
        myTable.columnModel.getColumn(5).preferredWidth = Column.DURATION.width
        myTable.columnModel.getColumn(6).preferredWidth = Column.STATUS.width
        myTable.model = tableModel
        table = myTable
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
        pomFileList.forEach {
            it.start = null
            it.finished = null
        }
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

    private fun targets(pomTargetList: List<JCheckBox>): List<String> {
        return pomTargetList.filter { it.isSelected }
                .map { it.name }
                .toList()
    }

    fun setVisible(visible: Boolean) {
        frmBuildtoolui.isVisible = true

    }

    /**
     * Initialize the contents of the frame.
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    fun initialize() {
        frmBuildtoolui = JFrame()
        frmBuildtoolui.foreground = Color.LIGHT_GRAY
        frmBuildtoolui.title = "Rob's BuildTool"
        frmBuildtoolui.setBounds(100, 100, 935, 631)
        frmBuildtoolui.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        val buttonPanel = JPanel()
        val flButtonPanel = buttonPanel.layout as FlowLayout
        flButtonPanel.hgap = 0
        flButtonPanel.alignment = FlowLayout.LEFT
        flButtonPanel.alignOnBaseline = true
        val optionsPanel = JPanel()
        optionsPanel.border = EtchedBorder(EtchedBorder.LOWERED, null, null)
        val statusPanel = JPanel()
        val flowLayout = statusPanel.layout as FlowLayout
        flowLayout.alignOnBaseline = true
        flowLayout.vgap = 0
        flowLayout.hgap = 0
        flowLayout.alignment = FlowLayout.LEFT
        statusPanel.border = BevelBorder(BevelBorder.LOWERED, null, null, null, null)
        val lblSelected = JLabel("Selected")
        lblSelected.font = Font("Arial", Font.PLAIN, 16)
        val optionsPanel_1 = JPanel()
        optionsPanel_1.border = EtchedBorder(EtchedBorder.LOWERED, null, null)
        val list: JList<*> = JList<Any?>()
        list.background = UIManager.getColor("Label.background")
        list.model = SelectedProjectsListModel(listOf())

        val gl_optionsPanel_1 = GroupLayout(optionsPanel_1)
        gl_optionsPanel_1.setHorizontalGroup(
                gl_optionsPanel_1.createParallelGroup(Alignment.LEADING)
                        .addGroup(gl_optionsPanel_1.createSequentialGroup()
                                .addComponent(list, GroupLayout.PREFERRED_SIZE, 204, GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt()))
        )
        gl_optionsPanel_1.setVerticalGroup(
                gl_optionsPanel_1.createParallelGroup(Alignment.LEADING)
                        .addComponent(list, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 306, Short.MAX_VALUE.toInt())
        )
        optionsPanel_1.layout = gl_optionsPanel_1
        val panel = JPanel()
        panel.border = EtchedBorder(EtchedBorder.LOWERED, null, null)
        val chckbxClean = JCheckBox("clean")
        chckbxClean.isSelected = true
        val chckbxInstall_1 = JCheckBox("install")
        chckbxInstall_1.isSelected = true
        val chckbxCompile = JCheckBox("compile")
        val chckbxTest = JCheckBox("test")
        val gl_panel = GroupLayout(panel)
        gl_panel.setHorizontalGroup(
                gl_panel.createParallelGroup(Alignment.LEADING)
                        .addGroup(gl_panel.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
                                        .addGroup(gl_panel.createSequentialGroup()
                                                .addComponent(chckbxClean)
                                                .addGap(53)
                                                .addComponent(chckbxTest, GroupLayout.DEFAULT_SIZE, 96, Short.MAX_VALUE.toInt()))
                                        .addComponent(chckbxCompile)
                                        .addComponent(chckbxInstall_1))
                                .addContainerGap())
        )
        gl_panel.setVerticalGroup(
                gl_panel.createParallelGroup(Alignment.LEADING)
                        .addGroup(gl_panel.createSequentialGroup()
                                .addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
                                        .addComponent(chckbxClean)
                                        .addComponent(chckbxTest))
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(chckbxCompile)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(chckbxInstall_1)
                                .addContainerGap(11, Short.MAX_VALUE.toInt()))
        )
        panel.layout = gl_panel
        val scrollPoms = JScrollPane()
        scrollPoms.toolTipText = "Pom files"

        scrollPoms.setViewportView(table)
        val btnRefresh = JButton("")

        btnRefresh.icon = createIcon("images/icon_refresh.png")
        btnRefresh.iconTextGap = 0
        btnRefresh.margin = Insets(0, 0, 0, 0)
        btnRefresh.isBorderPainted = true
        btnRefresh.isFocusPainted = false
        btnRefresh.isContentAreaFilled = false
        val lblMavenProjects = JLabel("Maven projects")
        lblMavenProjects.font = Font("Arial", Font.PLAIN, 16)
        val groupLayout = GroupLayout(frmBuildtoolui!!.contentPane)
        groupLayout.setHorizontalGroup(
                groupLayout.createParallelGroup(Alignment.TRAILING)
                        .addComponent(statusPanel, GroupLayout.DEFAULT_SIZE, 919, Short.MAX_VALUE.toInt())
                        .addGroup(Alignment.LEADING, groupLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                                        .addGroup(groupLayout.createSequentialGroup()
                                                .addComponent(lblMavenProjects, GroupLayout.PREFERRED_SIZE, 116, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(ComponentPlacement.RELATED, 544, Short.MAX_VALUE.toInt())
                                                .addComponent(btnRefresh))
                                        .addComponent(scrollPoms, GroupLayout.DEFAULT_SIZE, 680, Short.MAX_VALUE.toInt()))
                                .addPreferredGap(ComponentPlacement.UNRELATED)
                                .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                                        .addComponent(lblSelected, GroupLayout.PREFERRED_SIZE, 116, GroupLayout.PREFERRED_SIZE)
                                        .addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
                                                .addComponent(optionsPanel_1, GroupLayout.PREFERRED_SIZE, 209, GroupLayout.PREFERRED_SIZE)
                                                .addGroup(groupLayout.createParallelGroup(Alignment.TRAILING, false)
                                                        .addComponent(optionsPanel, 0, 0, Short.MAX_VALUE.toInt())
                                                        .addComponent(panel, GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE.toInt()))))
                                .addContainerGap())
                        .addGroup(Alignment.LEADING, groupLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(buttonPanel, GroupLayout.DEFAULT_SIZE, 899, Short.MAX_VALUE.toInt())
                                .addContainerGap())
        )
        groupLayout.setVerticalGroup(
                groupLayout.createParallelGroup(Alignment.LEADING)
                        .addGroup(groupLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                                        .addGroup(groupLayout.createSequentialGroup()
                                                .addComponent(panel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addComponent(optionsPanel, GroupLayout.PREFERRED_SIZE, 78, GroupLayout.PREFERRED_SIZE)
                                                .addGap(4)
                                                .addComponent(lblSelected, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addComponent(optionsPanel_1, GroupLayout.DEFAULT_SIZE, 299, Short.MAX_VALUE.toInt()))
                                        .addGroup(groupLayout.createSequentialGroup()
                                                .addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
                                                        .addComponent(lblMavenProjects, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(btnRefresh, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addComponent(scrollPoms, GroupLayout.DEFAULT_SIZE, 469, Short.MAX_VALUE.toInt())))
                                .addPreferredGap(ComponentPlacement.UNRELATED)
                                .addComponent(buttonPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addGap(17)
                                .addComponent(statusPanel, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        )

        val chckbxGitPull = JCheckBox("Git Pull")
        chckbxGitPull.toolTipText = "Perform a Git Pull on every git directory"
        val chckbxStopOnError = JCheckBox("Stop on error")
        chckbxStopOnError.toolTipText = "Stop building when a build fails"
        val chckbxOrderedBuild = JCheckBox("Ordered build")
        chckbxOrderedBuild.toolTipText = "Build projects in order or not"

        val glStatusPanel = GroupLayout(optionsPanel)
        glStatusPanel.setHorizontalGroup(
                glStatusPanel.createParallelGroup(Alignment.LEADING)
                        .addGroup(glStatusPanel.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(glStatusPanel.createParallelGroup(Alignment.LEADING)
                                        .addComponent(chckbxStopOnError, GroupLayout.PREFERRED_SIZE, 102, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(chckbxGitPull, GroupLayout.PREFERRED_SIZE, 72, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(chckbxOrderedBuild))
                                .addContainerGap(104, Short.MAX_VALUE.toInt()))
        )
        glStatusPanel.setVerticalGroup(
                glStatusPanel.createParallelGroup(Alignment.LEADING)
                        .addGroup(glStatusPanel.createSequentialGroup()
                                .addComponent(chckbxGitPull)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(chckbxStopOnError)
                                .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
                                .addComponent(chckbxOrderedBuild)
                                .addContainerGap())
        )
        optionsPanel.layout = glStatusPanel
        lbStatus.font = Font("Arial", Font.PLAIN, 11)
        statusPanel.add(lbStatus)

        buildButtons(buttonPanel)

        frmBuildtoolui.contentPane.layout = groupLayout
    }

    private fun buildButtons(buttonPanel: JPanel) {
        // Build button
        val btnBuild = JButton("Build")
        btnBuild.font = Font("Arial", Font.PLAIN, 14)

        val btnCancel = JButton("Cancel")
        btnCancel.isEnabled = false
        btnCancel.font = Font("Arial", Font.PLAIN, 14)

        buttonPanel.add(btnBuild)
        val horizontalStrut = Box.createHorizontalStrut(10)
        buttonPanel.add(horizontalStrut)
        buttonPanel.add(btnCancel)

        btnBuild.addActionListener {
            println("Build Build Build")
            btnCancel.isEnabled = true
            btnBuild.isEnabled = false
        }

        btnCancel.addActionListener {
            println("CANCEL!")
            btnCancel.isEnabled = false
            btnBuild.isEnabled = true
        }
    }
}