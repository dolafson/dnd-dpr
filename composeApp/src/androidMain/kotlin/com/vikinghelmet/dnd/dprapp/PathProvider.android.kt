package com.vikinghelmet.dnd.dprapp

import android.content.Context

private lateinit var applicationContext: Context

fun initContext(context: Context) {
    applicationContext = context
}

actual fun getDocumentsDirPath(): String {
    return applicationContext.filesDir.absolutePath
}