package nl.politie.buildtool.utils

import com.google.common.eventbus.EventBus
import org.springframework.stereotype.Component

@Component
class GlobalEventBus {
    val eventBus = EventBus()
}