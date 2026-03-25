package com.vikinghelmet.dnd.dpr

import java.util.logging.LogManager

class JulConfigurator() {
    init {
        try {
            val resourceFilename = "/logging.properties"
            println("attempting to load logging configuration from resource: $resourceFilename")
            // Load logging.properties from classpath
            val stream = JulConfigurator::class.java.getResourceAsStream(resourceFilename)
            if (stream != null) {
                println("resource found, loading configuration")
                LogManager.getLogManager().readConfiguration(stream)
            }
            else {
                println("resource not found: $resourceFilename")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}