package nl.politie.buildtool.gui

import com.google.common.eventbus.Subscribe
import nl.politie.buildtool.maven.BuildExecutor
import nl.politie.buildtool.model.BuildingCompleteEvent
import nl.politie.buildtool.model.Column
import nl.politie.buildtool.model.PomFile
import nl.politie.buildtool.model.PomFileTableModel
import nl.politie.buildtool.utils.*
import nl.politie.buildtool.utils.FileUtils.createIcon
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Component
import java.awt.Color
import java.awt.FlowLayout
import java.awt.Font
import java.awt.Insets
import java.io.IOException
import javax.swing.*
import javax.swing.GroupLayout.Alignment
import javax.swing.LayoutStyle.ComponentPlacement
import javax.swing.border.BevelBorder
import javax.swing.border.EtchedBorder


@Component
class BuildToolGUI(val directoryCrawler: DirectoryCrawler,
                   val buildExecutor: BuildExecutor,
                   val globalEventBus: GlobalEventBus) : InitializingBean {
    private val logger = LoggerFactory.getLogger(BuildToolGUI::class.java)

    private lateinit var frmBuildtoolui: JFrame
    private lateinit var tableModel: PomFileTableModel
    private var checkboxMap = mutableMapOf<String, JCheckBox>()
    private var table: JTable? = null
    private val btnBuild = JButton(TXT_BUILD)
    private val btnCancel = JButton(TXT_CANCEL)
    private var lbStatus = JLabel(LBL_POMS)
    private var pomFileList = listOf<PomFile>()
    private val pomTargetList = mutableListOf<JCheckBox>()
    private val selectedPomNamesListModel = DefaultListModel<String>()

    private fun initTable() {
        refreshPomFileList()
        tableModel = PomFileTableModel(pomFileList, selectedPomNamesListModel)
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

    private fun jcheckBoxAndAdd(text: String, tooltip: String, checked: Boolean = false): JCheckBox {
        val checkbox = jcheckBox(text, tooltip, checked)
        pomTargetList.add(checkbox)
        return checkbox
    }

    private fun jcheckBox(text: String, tooltip: String, checked: Boolean = false): JCheckBox {
        val checkbox = JCheckBox(text)
        checkbox.name = text
        checkbox.isSelected = checked
        checkbox.toolTipText = tooltip
        pomTargetList.add(checkbox)
        return checkbox
    }

    fun setVisible(visible: Boolean) {
        frmBuildtoolui.isVisible = true
    }


    private fun refreshButton(): JButton {
        val btnRefresh = JButton("")
        btnRefresh.icon = createIcon(ICON_REFRESH)
        btnRefresh.iconTextGap = 0
        btnRefresh.margin = Insets(0, 0, 0, 0)
        btnRefresh.isBorderPainted = true
        btnRefresh.isFocusPainted = false
        btnRefresh.isContentAreaFilled = false
        btnRefresh.addActionListener {
            refreshTable()
        }
        return btnRefresh
    }

    private fun refreshTable() {
        lbStatus.text = "Refreshing pom list..."

        refreshPomFileList()
        selectedPomNamesListModel.clear()
        tableModel = PomFileTableModel(pomFileList, selectedPomNamesListModel)
        table!!.model = tableModel
        tableModel.fireTableDataChanged()

        lbStatus.text = "Pom list refreshed. "
    }

    private fun refreshPomFileList() {
        pomFileList = directoryCrawler.getPomFileList()
    }

    private fun buildButtons(buttonPanel: JPanel) {
        // Build button
        btnBuild.font = Font("Arial", Font.PLAIN, 14)
        btnCancel.isEnabled = false
        btnCancel.font = Font("Arial", Font.PLAIN, 14)

        buttonPanel.add(btnBuild)
        val horizontalStrut = Box.createHorizontalStrut(10)
        buttonPanel.add(horizontalStrut)
        buttonPanel.add(btnCancel)

        btnBuild.addActionListener {
            println(" ==================== Build Build Build ==================== ")
            pomFileList.forEach { it.reset() }
            btnCancel.isEnabled = true
            btnBuild.isEnabled = false
            buildExecutor.executeBuild(selectedPomFileList(selectedPomNamesListModel, pomFileList), pomTargetList, tableModel)
        }

        btnCancel.addActionListener {
            println(" ==================== CANCEL ==================== ")
            btnCancel.isEnabled = false
            btnBuild.isEnabled = true
            buildExecutor.cancelBuild()
        }
    }

    private fun selectedPomFileList(selectedPomNamesListModel: DefaultListModel<String>, pomFileList: List<PomFile>): List<PomFile> {
        val returnList = mutableListOf<PomFile>()
        if (!selectedPomNamesListModel.isEmpty) {
            for (i in 0 until selectedPomNamesListModel.size()) {
                returnList.add(pomFileList.first { it.name == selectedPomNamesListModel.elementAt(i) })
            }
        }
        return returnList
    }

    @Subscribe
    fun updateStatusBar(event: String) {
        lbStatus.text = event
    }

    @Subscribe
    fun updateStatusComplete(event: BuildingCompleteEvent) {
        lbStatus.text = event.statusText
        if (btnCancel.isEnabled) {
            btnCancel.isEnabled = false
        }
        if (!btnBuild.isEnabled) {
            btnBuild.isEnabled = true
        }
    }

    override fun afterPropertiesSet() {
        initTable()
        initCheckboxes()
        globalEventBus.eventBus.register(this)
    }

    private fun initCheckboxes() {
        checkboxMap[TXT_CLEAN] = jcheckBoxAndAdd(TXT_CLEAN, TOOLTIP_CLEAN, true)
        checkboxMap[TXT_INSTALL] = jcheckBoxAndAdd(TXT_INSTALL, TOOLTIP_INSTALL, true)
        checkboxMap[TXT_COMPILE] = jcheckBoxAndAdd(TXT_COMPILE, TOOLTIP_COMPILE, false)
        checkboxMap[TXT_TEST] = jcheckBoxAndAdd(TXT_TEST, TOOLTIP_TEST, false)
        checkboxMap[TXT_GIT_PULL] = jcheckBox(TXT_GIT_PULL, TOOLTIP_GIT_PULL)
        checkboxMap[TXT_STOP_ON_ERROR] = jcheckBox(TXT_STOP_ON_ERROR, TOOLTIP_STOP_ON_ERROR)
        checkboxMap[TXT_ORDERED_BUILD] = jcheckBox(TXT_ORDERED_BUILD, TOOLTIP_ORDERED_BUILD)
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
        frmBuildtoolui.title = TITLE
        frmBuildtoolui.setBounds(100, 100, 935, 631)
        frmBuildtoolui.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frmBuildtoolui.iconImage = createIcon(ICON_BURGER).image

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
        val lblSelected = JLabel(LBL_SELECTED)
        lblSelected.font = Font("Arial", Font.PLAIN, 16)
        val optionsPanel_1 = JPanel()
        optionsPanel_1.border = EtchedBorder(EtchedBorder.LOWERED, null, null)

        val list = JList(selectedPomNamesListModel)
        list.background = UIManager.getColor("Label.background")

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


        val gl_panel = GroupLayout(panel)
        gl_panel.setHorizontalGroup(
                gl_panel.createParallelGroup(Alignment.LEADING)
                        .addGroup(gl_panel.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
                                        .addGroup(gl_panel.createSequentialGroup()
                                                .addComponent(checkboxMap[TXT_CLEAN])
                                                .addGap(53)
                                                .addComponent(checkboxMap[TXT_TEST], GroupLayout.DEFAULT_SIZE, 96, Short.MAX_VALUE.toInt()))
                                        .addComponent(checkboxMap[TXT_COMPILE])
                                        .addComponent(checkboxMap[TXT_INSTALL]))
                                .addContainerGap())
        )
        gl_panel.setVerticalGroup(
                gl_panel.createParallelGroup(Alignment.LEADING)
                        .addGroup(gl_panel.createSequentialGroup()
                                .addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
                                        .addComponent(checkboxMap[TXT_CLEAN])
                                        .addComponent(checkboxMap[TXT_TEST]))
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(checkboxMap[TXT_COMPILE])
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(checkboxMap[TXT_INSTALL])
                                .addContainerGap(11, Short.MAX_VALUE.toInt()))
        )
        panel.layout = gl_panel
        val scrollPoms = JScrollPane()
        scrollPoms.toolTipText = LBL_POM_FILES
        scrollPoms.setViewportView(table)

        val btnRefresh = refreshButton()

        val lblMavenProjects = JLabel(LBL_MAVEN_PROJECTS)
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


        val glStatusPanel = GroupLayout(optionsPanel)
        glStatusPanel.setHorizontalGroup(
                glStatusPanel.createParallelGroup(Alignment.LEADING)
                        .addGroup(glStatusPanel.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(glStatusPanel.createParallelGroup(Alignment.LEADING)
                                        .addComponent(checkboxMap[TXT_STOP_ON_ERROR], GroupLayout.PREFERRED_SIZE, 102, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(checkboxMap[TXT_GIT_PULL], GroupLayout.PREFERRED_SIZE, 72, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(checkboxMap[TXT_ORDERED_BUILD]))
                                .addContainerGap(104, Short.MAX_VALUE.toInt()))
        )
        glStatusPanel.setVerticalGroup(
                glStatusPanel.createParallelGroup(Alignment.LEADING)
                        .addGroup(glStatusPanel.createSequentialGroup()
                                .addComponent(checkboxMap[TXT_GIT_PULL])
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(checkboxMap[TXT_STOP_ON_ERROR])
                                .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
                                .addComponent(checkboxMap[TXT_ORDERED_BUILD])
                                .addContainerGap())
        )
        optionsPanel.layout = glStatusPanel
        lbStatus.font = Font("Arial", Font.PLAIN, 11)
        statusPanel.add(lbStatus)

        buildButtons(buttonPanel)

        frmBuildtoolui.contentPane.layout = groupLayout
    }
}