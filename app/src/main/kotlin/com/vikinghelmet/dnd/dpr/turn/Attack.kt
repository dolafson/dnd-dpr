package com.vikinghelmet.dnd.dpr.turn

import com.vikinghelmet.dnd.dpr.character.actions.ActionModifier
import com.vikinghelmet.dnd.dpr.monsters.Monster
import kotlinx.serialization.Serializable

@Serializable
data class Attack(
    // required fields
    val monster: Monster,
    val attack: AttackAction, // weapon or spell

    val actionModifiers: ArrayList<ActionModifier> = ArrayList(), // named, non-spell preconditions (eg Colossus Slayer)

    // optional fields
    var preconditions: Preconditions? = null,
    val isBonusAction: Boolean? = false
) {
    fun getLabel(): String {
        return if (actionModifiers.isEmpty()) attack.toString() else String.format(
            "%s%s",
            attack.toString(),
            actionModifiers
        )
    }

    fun copyProposedAttack(): Attack {
        return Attack(this.monster, this.attack, ArrayList(), null, isBonusAction)
    }
}