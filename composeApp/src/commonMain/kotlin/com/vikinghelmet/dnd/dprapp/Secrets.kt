package com.vikinghelmet.dnd.dprapp

object Secrets {
    var secrets: Map<String,String> = mutableMapOf()

    fun setProperties(propertyFileContents: String?) {
        if (propertyFileContents == null) return
        secrets = propertyFileContents.lines().filter { it.isNotBlank() }
            .associate { line ->
                val (key, value) = line.split("=")
                key to value
            }
    }

    fun getCsvUploadUrl(): String? {
        return secrets["csvUploadUrl"]
    }
}