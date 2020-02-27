package nl.politie.buildtool.model

import javax.swing.ListModel
import javax.swing.event.ListDataListener

class SelectedProjectsListModel(private val selectedPomList: List<PomFile>) : ListModel<PomFile> {
    override fun getElementAt(row: Int): PomFile = selectedPomList[row]

    override fun getSize(): Int = selectedPomList.size


    override fun addListDataListener(listDataListener: ListDataListener?) {
        // Not implemented
    }

    override fun removeListDataListener(listDataListener: ListDataListener?) {
        // Not implemented
    }
}