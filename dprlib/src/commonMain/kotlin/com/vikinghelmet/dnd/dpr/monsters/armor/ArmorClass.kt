package com.vikinghelmet.dnd.dpr.monsters.armor

import com.vikinghelmet.dnd.dpr.monsters.Condition
import kotlinx.serialization.Serializable

@Serializable
data class ArmorClass(
    val type: String,
    val value: Int,
    val desc: String?= null,
    val condition: Condition?= null,
    val spell: ArmorClassSpell ?= null,
    val armor: List<Armor> ?= emptyList(),
)