package nl.politie.buildtool.maven

import nl.politie.buildtool.model.*
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
    private var mavenHome = initMavenHome()
    private var cancelled = false
    private var currentLatch: CountDownLatch = CountDownLatch(1)
    private var executor = Executors.newSingleThreadExecutor()
    private var future: Future<*>? = null

    fun initMavenHome(): String {
        var mHome = System.getenv("M2_HOME")

        if (mHome == null) {
            mHome = System.getenv("MAVEN_HOME")
        }
        logger.info("Maven home found: $mavenHome")
        if (mHome == null) {
            throw NullPointerException("No maven home found. Define a M2_HOME or MAVEN_HOME environment variable first. ")
        }
        return mHome
    }

    fun invoke(pomFiles: List<PomFile>, targets: List<String>, tableModel: PomFileTableModel) {
        cancelled = false
        val invoker = DefaultInvoker()
        invoker.mavenHome = File(mavenHome)
        // set all to queued
        pomFiles.forEach { it.status = BuildStatus.QUEUED }
        tableModel.fireTableDataChanged()

        // Start one by one
        pomFiles.forEach {
            var endStatus = invokePom(it, targets, invoker, tableModel)
            if (endStatus != null &&
                    endStatus == BuildStatus.FAIL &&
                    Globals.isStopOnError()) {
                globalEventBus.eventBus.post(BuildingCompleteEvent("Building stopped because previous build failed. "))
            }
        }

        globalEventBus.eventBus.post(BuildingCompleteEvent("Building complete. "))
    }

    private fun invokePom(it: PomFile, targets: List<String>, invoker: DefaultInvoker, tableModel: PomFileTableModel): BuildStatus? {
        var endStatus: BuildStatus? = null
        if (!cancelled) {
            postMessage("Execute ${it.name}, target=$targets")
            currentLatch = CountDownLatch(1)
            future = executor.submit {
                endStatus = invoke(it, targets, invoker, tableModel)
                currentLatch.countDown()
            }

            currentLatch.await()
        }
        return endStatus
    }

    private fun invoke(pomFile: PomFile, targets: List<String>, invoker: DefaultInvoker, tableModel: PomFileTableModel): BuildStatus? {
        // set to building
        pomFile.start = LocalDateTime.now()
        pomFile.status = BuildStatus.BUILDING
        postMessage("${pomFile.name} status ${pomFile.status}")
        tableModel.fireRowUpdated(pomFile)

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
        pomFile.executionException = result.executionException
        pomFile.finished = LocalDateTime.now()
        pomFile.durationOfLastBuild = Duration.between(pomFile.start, pomFile.finished)
        postMessage("${pomFile.name} status ${pomFile.status}. Duration was ${pomFile.durationOfLastBuild}")

        tableModel.fireRowUpdated(pomFile)
        return pomFile.status
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