package com.vikinghelmet.dnd.dpr.turn

import kotlinx.serialization.Serializable

@Serializable
data class Turn(
    val preconditions: Preconditions? = null,
    val attacks: List<Attack>,
    val notes: List<String>? = null,
) {
}