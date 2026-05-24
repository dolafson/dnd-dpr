package com.vikinghelmet.dnd.dpr.monsters.actions

import kotlinx.serialization.Serializable

@Serializable
data class ActionOptions(
    val choose: Int,
    val from: ActionOptionsFrom,
    val type: String
)