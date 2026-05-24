package com.vikinghelmet.dnd.dpr.monsters

import com.vikinghelmet.dnd.dpr.monsters.damage.Damage
import com.vikinghelmet.dnd.dpr.monsters.spells.Spellcasting
import kotlinx.serialization.Serializable

@Serializable
data class SpecialAbility(
    val dc: Dc ?= null,
    val desc: String,
    val name: String,
    val spellcasting: Spellcasting?= null,
    val usage: Usage ?= null,
    val damage: List<Damage> ?= emptyList(),
)