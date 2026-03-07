package com.vikinghelmet.dnd.dpr.turn

import com.vikinghelmet.dnd.dpr.character.actions.ActionModifier
import com.vikinghelmet.dnd.dpr.monsters.Monster
import kotlinx.serialization.Serializable

@Serializable
data class Attack(
    // required fields
    val monster: Monster,
    val action: AttackAction, // weapon or spell

    val actionModifiers: ArrayList<ActionModifier> = ArrayList(), // named, non-spell preconditions (eg Colossus Slayer)

    // optional fields
    var preconditions: Preconditions? = null,
    val isBonusAction: Boolean? = false
) {
    fun getLabel(): String {
        return if (actionModifiers.isEmpty()) action.toString() else String.format(
            "%s%s",
            action.toString(),
            actionModifiers
        )
    }
}