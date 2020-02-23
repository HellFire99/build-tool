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
              var status: BuildStatus? = null)