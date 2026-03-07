package com.vikinghelmet.dnd.dpr.turn

import kotlinx.serialization.Serializable

@Serializable
data class Turn(
    val attacks: List<Attack>,
) {
    fun copyProposedTurn(): Turn {
        return Turn (attacks.map { a -> a.copyProposedAttack() }.toMutableList())
    }
}