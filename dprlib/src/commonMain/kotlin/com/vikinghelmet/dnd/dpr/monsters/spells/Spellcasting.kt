package com.vikinghelmet.dnd.dpr.monsters.spells

import kotlinx.serialization.Serializable

@Serializable
data class Spellcasting(
    val ability: Ability,
    val components_required: List<String>,

    val dc: Int? = 0,
    val level: Int? = 0,
    val modifier: Int? = 0,
    val school: String? = null,
    val slots: Map<String,Int> = emptyMap(),
    val spells: List<Spell>
)