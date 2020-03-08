package nl.politie.buildtool.gui

import nl.politie.buildtool.model.PomFileTableModel
import java.awt.Component
import javax.swing.JTable
import javax.swing.table.DefaultTableCellRenderer


class MyTableCellRenderer : DefaultTableCellRenderer() {
    override fun getTableCellRendererComponent(table: JTable, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component? {
        val model: PomFileTableModel = table.model as PomFileTableModel
        val c: Component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
        c.background = model.getRowColor(row)
        return c
    }
}