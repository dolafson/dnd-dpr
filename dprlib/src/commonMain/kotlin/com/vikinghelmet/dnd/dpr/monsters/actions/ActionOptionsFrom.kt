package com.vikinghelmet.dnd.dpr.monsters.actions

import kotlinx.serialization.Serializable

@Serializable
data class ActionOptionsFrom(
    val option_set_type: String,
    val options: List<ActionOptionsOption>
)