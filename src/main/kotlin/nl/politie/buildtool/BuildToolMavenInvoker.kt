package nl.politie.buildtool

import nl.politie.buildtool.model.PomFile
import org.apache.maven.shared.invoker.DefaultInvocationRequest
import org.apache.maven.shared.invoker.DefaultInvoker
import org.springframework.stereotype.Component
import java.io.File

@Component
class BuildToolMavenInvoker {
    fun invoke(pomFile: PomFile, targets: List<String>) {
        val request = DefaultInvocationRequest()
        request.pomFile = pomFile.file
        request.goals = targets

        val invoker = DefaultInvoker()
        val mavenHome = System.getenv("MAVEN_HOME")
        invoker.mavenHome = File(mavenHome)
        invoker.execute(request)
    }
}