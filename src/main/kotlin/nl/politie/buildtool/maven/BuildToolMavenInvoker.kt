package nl.politie.buildtool.maven

import nl.politie.buildtool.model.PomFile
import org.apache.maven.shared.invoker.DefaultInvocationRequest
import org.apache.maven.shared.invoker.DefaultInvoker
import org.springframework.stereotype.Component
import java.io.File

@Component
class BuildToolMavenInvoker {
    fun invoke(pomFiles: List<PomFile>, targets: List<String>) {
        val invoker = DefaultInvoker()
        val mavenHome = System.getenv("MAVEN_HOME")
        invoker.mavenHome = File(mavenHome)
        pomFiles.forEach {
            invoke(it, targets, invoker)
        }
    }

    private fun invoke(pomFile: PomFile, targets: List<String>, invoker: DefaultInvoker) {
        val request = DefaultInvocationRequest()
        request.pomFile = pomFile.file
        request.goals = targets
        invoker.execute(request)
    }
}