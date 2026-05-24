package com.vikinghelmet.dnd.dpr.monsters.actions

import com.vikinghelmet.dnd.dpr.monsters.Dc
import com.vikinghelmet.dnd.dpr.monsters.damage.Damage
import kotlinx.serialization.Serializable

@Serializable
data class LegendaryAction(
    val damage: List<Damage> ?= emptyList(),
    val desc: String,
    val name: String,
    val dc: Dc?= null,
    val attack_bonus: Int? = 0,
)