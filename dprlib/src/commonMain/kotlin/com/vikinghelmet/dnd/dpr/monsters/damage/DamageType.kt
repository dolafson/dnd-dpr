package com.vikinghelmet.dnd.dpr.monsters.damage

import kotlinx.serialization.Serializable

@Serializable
data class DamageType(
    val index: String,
    val name: String,
    val url: String
)