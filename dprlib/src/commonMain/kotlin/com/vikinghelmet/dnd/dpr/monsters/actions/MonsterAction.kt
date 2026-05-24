package com.vikinghelmet.dnd.dpr.monsters.actions

import com.vikinghelmet.dnd.dpr.monsters.Attack
import com.vikinghelmet.dnd.dpr.monsters.Dc
import com.vikinghelmet.dnd.dpr.monsters.Usage
import com.vikinghelmet.dnd.dpr.monsters.damage.Damage
import kotlinx.serialization.Serializable

@Serializable
data class ActionX(
    val action_name: String,
    val count: Int,
    val type: String
)

@Serializable
data class MonsterAction(
    val name: String,
    val desc: String,

    // sadly, everything in here is optional except for name and desc; for multiattack all other fields are missing
    val attacks: List<Attack> ?= emptyList(),
    val actions: List<ActionX> ?= emptyList(),
    val attack_bonus: Int ?= 0,
    val damage: List<Damage> ?= emptyList(),
    val dc: Dc?= null,
    val multiattack_type: String ?= null,
    val options: ActionOptions ?= null,
    val action_options: ActionOptions ?= null,
    val usage: Usage?= null
)