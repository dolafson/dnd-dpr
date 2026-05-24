package com.vikinghelmet.dnd.dpr.monsters

import kotlinx.serialization.Serializable

@Serializable
data class Form(
    val index: String,
    val name: String,
    val url: String
)