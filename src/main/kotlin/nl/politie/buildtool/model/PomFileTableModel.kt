package nl.politie.buildtool.model

import org.springframework.util.ResourceUtils
import java.time.Duration
import java.time.format.DateTimeFormatter
import javax.swing.ImageIcon
import javax.swing.table.AbstractTableModel


class PomFileTableModel(private val pomFileList: List<PomFile>) : AbstractTableModel() {
    private val columnNames = arrayOf(
            Column.CHECKED,
            Column.NAME,
            Column.VERSION,
            Column.START,
            Column.FINISHED,
            Column.DURATION,
            Column.STATUS)

    var statusMap: Map<BuildStatus, ImageIcon>

    init {
        statusMap = mapOf(
                BuildStatus.QUEUED to createIcon("images/queued.png"),
                BuildStatus.BUILDING to createIcon("images/building.png"),
                BuildStatus.SUCCESS to createIcon("images/check.gif"),
                BuildStatus.FAIL to createIcon("images/error.png"),
                BuildStatus.NONE to createIcon("images/none.png")
        )
    }

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
        val ms = durationOfLastBuild.toMillis().toString().padStart(2, '0')
        return "${min}:${sec}:${ms}"

    }

    override fun isCellEditable(row: Int, col: Int): Boolean { //Note that the data/cell address is constant,
        return col == 0
    }

    override fun setValueAt(value: Any?, row: Int, col: Int) {
        if (col == 0 && value is Boolean) {
            pomFileList[row].checked = value
        }
        fireTableCellUpdated(row, col)
    }


    override fun getColumnName(col: Int) = columnNames[col].visibleName


    override fun getColumnClass(col: Int) = getValueAt(0, col).javaClass

    private fun createIcon(path: String) =
            ImageIcon(ResourceUtils.getFile("classpath:${path}").readBytes())

}