package com.vikinghelmet.dnd.dpr.scenario

import com.vikinghelmet.dnd.dpr.spells.Spell
import com.vikinghelmet.dnd.dpr.turn.AttackAction

data class ActionsAvailable(
    val meleeActionList: MutableList<AttackAction> = mutableListOf(),
    val rangedActionList: MutableList<AttackAction> = mutableListOf()
) {
    fun getFullList(isMelee: Boolean): List<AttackAction> {
        return if (isMelee) meleeActionList else rangedActionList
    }

    fun add(range: Int, attackAction: AttackAction) {
        val both = (attackAction is Spell) && attackAction.triggersSavingThrow()

        if (both || range <= 5) {
            meleeActionList.add(attackAction)
        }
        else {
            rangedActionList.add(attackAction)
        }
    }
}