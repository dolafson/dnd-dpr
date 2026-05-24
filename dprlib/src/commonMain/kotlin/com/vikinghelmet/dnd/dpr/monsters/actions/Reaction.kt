package com.vikinghelmet.dnd.dpr.monsters.actions

import com.vikinghelmet.dnd.dpr.monsters.Dc
import kotlinx.serialization.Serializable

@Serializable
data class Reaction(
    val name: String,
    val desc: String,
    val dc: Dc?= null,
)