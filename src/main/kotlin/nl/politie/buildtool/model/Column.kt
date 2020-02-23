package nl.politie.buildtool.model

import java.time.LocalDateTime
import javax.swing.ImageIcon

enum class Column(val visibleName: String, val clazz: Class<*>, val width: Int) {
    CHECKED("", Boolean::class.java, 10),
    NAME("name", String::class.java, 100),
    VERSION("version", String::class.java, 120),
    START("start", LocalDateTime::class.java, 30),
    FINISHED("finished", LocalDateTime::class.java, 30),
    DURATION("duration", String::class.java, 30),
    STATUS("status", ImageIcon::class.java, 30)
}