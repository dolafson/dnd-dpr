package com.vikinghelmet.dnd.dpr.spells

import kotlinx.serialization.Serializable

@Serializable
data class SubclassSpellsPrepared(
    val subclass: String,
    val level: Int,
    val spells: List<String>,
)