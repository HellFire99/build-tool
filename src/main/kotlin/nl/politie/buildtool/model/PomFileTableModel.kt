package nl.politie.buildtool.model

import java.time.Duration
import java.time.format.DateTimeFormatter
import javax.swing.table.AbstractTableModel


class PomFileTableModel(val pomFileList: List<PomFile>) : AbstractTableModel() {
    private val columnNames = arrayOf(
            Column.CHECKED,
            Column.NAME,
            Column.VERSION,
            Column.START,
            Column.FINISHED,
            Column.DURATION,
            Column.STATUS)

    override fun getRowCount() = pomFileList.size


    override fun getColumnCount() = columnNames.size


    override fun getValueAt(row: Int, col: Int): Any {
        return when (col) {
            0 -> pomFileList[row].checked
            1 -> pomFileList[row].name
            2 -> pomFileList[row].version
            3 -> pomFileList[row].start?.format(DateTimeFormatter.ISO_LOCAL_TIME) ?: ""
            4 -> pomFileList[row].finished?.format(DateTimeFormatter.ISO_LOCAL_TIME) ?: ""
            5 -> formatDuration(pomFileList[row].durationOfLastBuild)
            6 -> pomFileList[row].status ?: ""
            else -> ""
        }
    }

    private fun formatDuration(durationOfLastBuild: Duration?): String {
        if (durationOfLastBuild == null) {
            return ""
        }
        return "${durationOfLastBuild.toMinutes()}:${durationOfLastBuild.toSeconds()}:${durationOfLastBuild.toMillis()}"

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


}