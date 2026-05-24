package com.vikinghelmet.dnd.dpr.monsters.damage

import kotlinx.serialization.Serializable

@Serializable
data class DamageFrom(
    val option_set_type: String,
    val options: List<DamageOption>
)