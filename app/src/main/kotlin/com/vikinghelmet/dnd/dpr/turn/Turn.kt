package com.vikinghelmet.dnd.dpr.turn

import kotlinx.serialization.Serializable

@Serializable
data class Turn(
    val preconditions: Preconditions? = null,
    val attacks: List<Attack>,
    val notes: List<String>? = null,
) {
    fun shortString(): String {
        val result = ArrayList<String>()
        for (a in attacks)  result.add(a.attack)
        return result.toString()
    }
}