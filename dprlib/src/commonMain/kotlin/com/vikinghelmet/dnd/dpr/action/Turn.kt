package com.vikinghelmet.dnd.dpr.action

import com.vikinghelmet.dnd.dpr.scenario.combat.ActionGoal
import com.vikinghelmet.dnd.dpr.spells.Spell
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

    fun includesBA() = attacks.any { it.isBonusAction == true }

    fun matchesGoal(goal: ActionGoal): Boolean {
        val spell = getSpell()
        if (spell == null || !spell.isHealing()) {
            return (goal == ActionGoal.Attack)
        }
        return (goal == ActionGoal.Heal)
    }

    fun getSpell(): Spell? {
        val spellAttack = attacks.firstOrNull { it.action is Spell } ?: return null
        return spellAttack.action as Spell
    }

}