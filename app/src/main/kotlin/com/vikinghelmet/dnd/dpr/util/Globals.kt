package com.vikinghelmet.dnd.dpr.util

object Globals {
    var debug = false
    fun debug(str: String) { if (debug) System.err.println(str) }
}