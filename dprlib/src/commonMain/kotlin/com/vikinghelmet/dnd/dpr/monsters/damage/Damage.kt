package com.vikinghelmet.dnd.dpr.monsters.damage

import com.vikinghelmet.dnd.dpr.monsters.Dc
import kotlinx.serialization.Serializable

@Serializable
data class Damage(
    // most times damage type is constant
    val damage_dice: String?= null,
    val damage_type: DamageType?= null,
    val dc: Dc?= null,

    // sometimes you get to choose the damage ...
    val choose: Int ?= 0,
    val from: DamageFrom ?= null,
    val type: String ?= null
)