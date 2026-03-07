package com.vikinghelmet.dnd.dpr.turn

import com.vikinghelmet.dnd.dpr.character.actions.ActionModifier
import kotlinx.serialization.Serializable

@Serializable
data class Attack(
    // required fields
    val monster: String,
    val attack: String, // name of spell or weapon

    val actionModifiers: ArrayList<ActionModifier> = ArrayList(), // named, non-spell preconditions (eg Colossus Slayer)

    // optional fields
    var preconditions: Preconditions? = null,
    val isBonusAction: Boolean? = false,
    val notes: List<String>? = null,
    val numTargets: Int? = 1
) {
    fun getLabel(): String {
        return if (actionModifiers.isEmpty()) attack else String.format("%s%s", attack, actionModifiers)
    }
}