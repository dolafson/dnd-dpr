package com.vikinghelmet.dnd.dprapp

actual fun getDocumentsDirPath(): String {
    return System.getProperty("user.home")
}
