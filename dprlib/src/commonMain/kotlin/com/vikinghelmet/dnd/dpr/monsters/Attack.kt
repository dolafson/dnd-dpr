package com.vikinghelmet.dnd.dpr.monsters

import com.vikinghelmet.dnd.dpr.monsters.damage.Damage
import kotlinx.serialization.Serializable

@Serializable
data class Attack(
    val name: String,
    val dc: Dc ?= null,
    val damage: List<Damage> ?= emptyList(),
    )