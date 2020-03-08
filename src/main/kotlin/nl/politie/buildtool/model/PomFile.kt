package nl.politie.buildtool.model

import java.io.File
import java.time.Duration
import java.time.LocalDateTime

class PomFile(val name: String,
              val version: String,
              val file: File,
              var checked: Boolean = false,
              var start: LocalDateTime? = null,
              var finished: LocalDateTime? = null,
              var durationOfLastBuild: Duration? = null,
              var status: BuildStatus? = null) {
    fun reset() {
        start = null
        finished = null
        durationOfLastBuild = null
        status = null
    }

    override fun toString(): String {
        return "PomFile(name='$name', version='$version', file=$file)"
    }
}