package com.vikinghelmet.dnd.dpr.monsters

import kotlinx.serialization.Serializable

@Serializable
data class ConditionImmunity(
    val index: String,
    val name: String,
    val url: String
)