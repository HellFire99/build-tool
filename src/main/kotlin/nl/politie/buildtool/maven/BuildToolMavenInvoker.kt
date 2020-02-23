package nl.politie.buildtool.maven

import nl.politie.buildtool.model.BuildStatus
import nl.politie.buildtool.model.PomFile
import nl.politie.buildtool.model.PomFileTableModel
import org.apache.maven.shared.invoker.DefaultInvocationRequest
import org.apache.maven.shared.invoker.DefaultInvoker
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.File
import java.time.Duration
import java.time.LocalDateTime

@Component
class BuildToolMavenInvoker {
    private val logger = LoggerFactory.getLogger(BuildToolMavenInvoker::class.java)
    fun invoke(pomFiles: List<PomFile>, targets: List<String>, tableModel: PomFileTableModel) {
        val invoker = DefaultInvoker()
        val mavenHome = System.getenv("MAVEN_HOME")
        invoker.mavenHome = File(mavenHome)
        // set all to queued
        pomFiles.forEach { it.status = BuildStatus.QUEUED }

        // Start one by one
        pomFiles.forEach {
            invoke(it, targets, invoker, tableModel)
        }
    }

    private fun invoke(pomFile: PomFile, targets: List<String>, invoker: DefaultInvoker, tableMModel: PomFileTableModel) {
        // set to building
        pomFile.start = LocalDateTime.now()
        pomFile.status = BuildStatus.BUILDING
        logger.info("${pomFile.name} status ${pomFile.status}")
        tableMModel.fireTableDataChanged()

        // Invoke maven
        val request = DefaultInvocationRequest()
        request.pomFile = pomFile.file
        request.goals = targets
        val result = invoker.execute(request)

        // Set result status
        pomFile.status = if (result.exitCode == 0)
            (BuildStatus.SUCCESS) else {
            BuildStatus.FAIL
        }
        pomFile.finished = LocalDateTime.now()
        pomFile.durationOfLastBuild = Duration.between(pomFile.start, pomFile.finished)
        logger.info("${pomFile.name} status ${pomFile.status}. Duration was ${pomFile.durationOfLastBuild}")
        tableMModel.fireTableDataChanged()
    }
}