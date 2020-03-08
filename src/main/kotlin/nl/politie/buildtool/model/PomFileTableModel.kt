package nl.politie.buildtool.model

import nl.politie.buildtool.utils.FileUtils.createIcon
import java.time.Duration
import java.time.format.DateTimeFormatter
import javax.swing.DefaultListModel
import javax.swing.ImageIcon
import javax.swing.table.AbstractTableModel


class PomFileTableModel(var pomFileList: List<PomFile>, private val selectedPomList: DefaultListModel<String>) : AbstractTableModel() {
    private val columnNames = arrayOf(
            Column.CHECKED,
            Column.NAME,
            Column.VERSION,
            Column.START,
            Column.FINISHED,
            Column.DURATION,
            Column.STATUS)

    var statusMap: Map<BuildStatus, ImageIcon> = mapOf(
            BuildStatus.QUEUED to createIcon(ICON_QUEUED),
            BuildStatus.BUILDING to createIcon(ICON_BUILDING),
            BuildStatus.SUCCESS to createIcon(ICON_CHECK),
            BuildStatus.FAIL to createIcon(ICON_ERROR),
            BuildStatus.NONE to createIcon(ICON_NONE)
    )

    override fun getRowCount() = pomFileList.size


    override fun getColumnCount() = columnNames.size


    override fun getValueAt(row: Int, col: Int): Any {
        return when (col) {
            0 -> pomFileList[row].checked
            1 -> pomFileList[row].name
            2 -> pomFileList[row].version
            3 -> pomFileList[row].start?.format(DateTimeFormatter.ofPattern("HH:mm:ss")) ?: ""
            4 -> pomFileList[row].finished?.format(DateTimeFormatter.ofPattern("HH:mm:ss")) ?: ""
            5 -> formatDuration(pomFileList[row].durationOfLastBuild)
            6 -> formatStatus(pomFileList[row].status)
            else -> ""
        }
    }

    private fun formatStatus(status: BuildStatus?): ImageIcon {
        return if (status == null) {
            statusMap[BuildStatus.NONE] ?: error("BuildStatus not found in map.")
        } else {
            statusMap[status] ?: error("BuildStatus not found in map.")
        }
    }

    private fun formatDuration(durationOfLastBuild: Duration?): String {
        if (durationOfLastBuild == null) {
            return ""
        }
        val min = durationOfLastBuild.toMinutes().toString().padStart(2, '0')
        val sec = durationOfLastBuild.toSeconds().toString().padStart(2, '0')
        var ms = durationOfLastBuild.toMillis().toString().padStart(2, '0')
        if (ms.length > 2) {
            ms = ms.substring(0, 2)
        }

        return "${min}:${sec}:${ms}"

    }

    override fun isCellEditable(row: Int, col: Int): Boolean { //Note that the data/cell address is constant,
        return col == 0
    }

    override fun setValueAt(value: Any?, row: Int, col: Int) {
        if (col == 0 && value is Boolean) {
            pomFileList[row].checked = value
            if (pomFileList[row].checked) {
                selectedPomList.addElement(pomFileList[row].name)
            } else {
                selectedPomList.removeElement(pomFileList[row].name)
            }
        }
        fireTableCellUpdated(row, col)
    }


    override fun getColumnName(col: Int) = columnNames[col].visibleName


    override fun getColumnClass(col: Int) = getValueAt(0, col).javaClass

    fun fireRowUpdated(pomFile: PomFile) {
        val index = pomFileList.indexOf(pomFile)
        fireTableRowsUpdated(index, index)
    }


}