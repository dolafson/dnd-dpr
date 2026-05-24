package com.vikinghelmet.dnd.dpr.monsters.armor

import kotlinx.serialization.Serializable

@Serializable
data class Armor(
    val index: String,
    val name: String,
    val url: String
)