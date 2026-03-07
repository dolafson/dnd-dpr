package com.vikinghelmet.dnd.dpr.turn

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

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

    fun deepCopy(): Turn {
        return Json.decodeFromString(Json.encodeToString(this)) // a little inefficient, but simple
    }
}