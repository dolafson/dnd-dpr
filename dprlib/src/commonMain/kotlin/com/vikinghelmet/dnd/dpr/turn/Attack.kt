package com.vikinghelmet.dnd.dpr.turn

import com.vikinghelmet.dnd.dpr.character.Combatant
import com.vikinghelmet.dnd.dpr.character.actions.ActionModifier
import kotlinx.serialization.Serializable

@Serializable
data class Attack(
    // required fields
    val target: Combatant,
    val action: AttackAction, // weapon or spell

    val actionModifiers: MutableList<ActionModifier> = mutableListOf(), // named, non-spell preconditions (eg Colossus Slayer)

    // optional fields
    val isBonusAction: Boolean? = false
) {
    override fun equals(other: Any?): Boolean {
        if (other == null || other !is Attack) return false
        if (this.target != other.target) return false
        if (this.action != other.action) return false
        // TODO: other fields ?
        return true
    }

    fun getLabel(): String {
        return action.toString().replace(",.*".toRegex(),"") +
            (if (actionModifiers.isNotEmpty()) actionModifiers else "")
    }

    override fun hashCode(): Int {
        var result = isBonusAction?.hashCode() ?: 0
        result = 31 * result + target.hashCode()
        result = 31 * result + action.hashCode()
        result = 31 * result + actionModifiers.hashCode()
        return result
    }
}