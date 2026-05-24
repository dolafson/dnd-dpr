package com.vikinghelmet.dnd.dpr.monsters.damage

import kotlinx.serialization.Serializable

@Serializable
data class DamageOption(
    val damage_dice: String,
    val damage_type: DamageType,
    val option_type: String,
    val notes: String?= null,
)