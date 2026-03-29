package com.vikinghelmet.dnd.dpr.turn

import kotlinx.serialization.Serializable

@Serializable
data class Turn(val attacks: List<Attack>) {
    override fun equals(other: Any?): Boolean {
        if (other == null || other !is Turn) return false
        return attacks.size == other.attacks.size && attacks.containsAll(other.attacks) && other.attacks.containsAll(attacks)
    }

    override fun hashCode(): Int {
        return attacks.hashCode()
    }

}