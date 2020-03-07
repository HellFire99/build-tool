package nl.politie.buildtool.maven

import nl.politie.buildtool.model.BuildStatus
import nl.politie.buildtool.model.BuildingCompleteEvent
import nl.politie.buildtool.model.PomFile
import nl.politie.buildtool.model.PomFileTableModel
import nl.politie.buildtool.utils.GlobalEventBus
import org.apache.maven.shared.invoker.DefaultInvocationRequest
import org.apache.maven.shared.invoker.DefaultInvoker
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.File
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.Future

@Component
class BuildToolMavenInvoker(val globalEventBus: GlobalEventBus) {

    private val logger = LoggerFactory.getLogger(BuildToolMavenInvoker::class.java)
    private val mavenHome = System.getenv("MAVEN_HOME")
    private var cancelled = false
    private var currentLatch: CountDownLatch = CountDownLatch(1)
    private var executor = Executors.newSingleThreadExecutor()
    private var future: Future<*>? = null

    fun invoke(pomFiles: List<PomFile>, targets: List<String>, tableModel: PomFileTableModel) {
        cancelled = false
        val invoker = DefaultInvoker()
        invoker.mavenHome = File(mavenHome)
        // set all to queued
        pomFiles.forEach { it.status = BuildStatus.QUEUED }

        // Start one by one
        pomFiles.forEach {
            invokePom(it, targets, invoker, tableModel)
        }

        globalEventBus.eventBus.post(BuildingCompleteEvent("Building complete. "))
    }

    private fun invokePom(it: PomFile, targets: List<String>, invoker: DefaultInvoker, tableModel: PomFileTableModel) {
        if (!cancelled) {
            postMessage("Execute ${it.name}, target=$targets")
            currentLatch = CountDownLatch(1)
            future = executor.submit {
                invoke(it, targets, invoker, tableModel)
                currentLatch.countDown()
            }

            currentLatch.await()
        }
    }

    private fun invoke(pomFile: PomFile, targets: List<String>, invoker: DefaultInvoker, tableModel: PomFileTableModel) {
        // set to building
        pomFile.start = LocalDateTime.now()
        pomFile.status = BuildStatus.BUILDING
        postMessage("${pomFile.name} status ${pomFile.status}")
        tableModel.fireTableDataChanged()

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
        postMessage("${pomFile.name} status ${pomFile.status}. Duration was ${pomFile.durationOfLastBuild}")

        tableModel.fireTableDataChanged()
    }

    fun cancelBuild() {
        cancelled = true
        if (future != null && !future!!.isDone) {
            future!!.cancel(true)
        }
        if (currentLatch.count == 1L) {
            currentLatch.countDown()
        }
    }

    private fun postMessage(message: String) {
        logger.info(message)
        globalEventBus.eventBus.post(message)
    }
}