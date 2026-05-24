package com.vikinghelmet.dnd.dpr.monsters.spells

import kotlinx.serialization.Serializable

@Serializable
data class Ability(
    val index: String,
    val name: String,
    val url: String
)