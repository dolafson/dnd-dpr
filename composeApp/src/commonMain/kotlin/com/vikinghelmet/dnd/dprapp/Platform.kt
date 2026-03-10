package com.vikinghelmet.dnd.dprapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform