package com.vikinghelmet.dnd.dpr.monsters.spells

import com.vikinghelmet.dnd.dpr.monsters.Usage
import kotlinx.serialization.Serializable

@Serializable
data class Spell(
    val level: Int,
    val name: String,
    val url: String,
    val usage: Usage? = null,
    val notes: String?= null,
)