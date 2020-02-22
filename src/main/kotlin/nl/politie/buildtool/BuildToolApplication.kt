package nl.politie.buildtool

import nl.politie.buildtool.gui.BuildToolGUI
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import javax.swing.SwingUtilities

@SpringBootApplication
class BuildToolApplication {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            var builder = SpringApplicationBuilder(BuildToolApplication::class.java)
            builder.web(WebApplicationType.NONE)
            builder.headless(false)
            var ctx = builder.run(*args);

            SwingUtilities.invokeLater {
                var buildToolUI = ctx.getBean(BuildToolGUI::class.java)
                buildToolUI.initialize()
                buildToolUI.setVisible(true)
            }
        }
    }


}


