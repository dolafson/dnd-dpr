package com.vikinghelmet.dnd.dpr.scenario.combat

import com.vikinghelmet.dnd.dpr.action.Combatant
import com.vikinghelmet.dnd.dpr.spells.Spell

data class SpellCast(
    val caster: Combatant,
    val spell: Spell,
    val turnCast: Int,
    var turnEnded: Int? = null,
) {
    init {
        if ((spell.getDuration() ?: 0) <= 1) turnEnded = turnCast
    }

    fun isStillRunning() = turnEnded == null

    fun isExpired(currentTurnId: Int): Boolean { // only returns true on the turn where the spell ends
        if (!isStillRunning()) return false
        val duration = (spell.getDuration() ?: 0)
        if (currentTurnId < turnCast + duration) return false
        turnEnded = currentTurnId
        return true
    }
}

