package com.vikinghelmet.dnd.dpr.monsters.actions

import com.vikinghelmet.dnd.dpr.monsters.Dc
import com.vikinghelmet.dnd.dpr.monsters.damage.Damage
import kotlinx.serialization.Serializable

@Serializable
data class ActionOptionsOption(
    val damage: List<Damage> ?= emptyList(),
    val dc: Dc?= null,
    val items: List<ActionOptionsOption> ?= emptyList(),
    val name: String ?= null,
    val action_name: String ?= null,
    val desc: String ?= null,
    val type: String ?= null,
    val count: Int ?= 0,
    val option_type: String
)