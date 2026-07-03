package com.vikinghelmet.dnd.dpr.util

import dev.shivathapaa.logger.api.LoggerFactory

object LogOnce {
    private val logger = LoggerFactory.get("LogOnce")
    private val seen = mutableSetOf<String>()

    fun error(message: String) {
        if (message in seen) return
        seen.add(message)
        logger.error { message }
    }

    fun warn(message: String) {
        if (message in seen) return
        seen.add(message)
        logger.warn { message }
    }
}
