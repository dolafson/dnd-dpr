package com.vikinghelmet.dnd.dpr.scenario.onesided

import com.vikinghelmet.dnd.dpr.action.Combatant
import com.vikinghelmet.dnd.dpr.action.Turn
import com.vikinghelmet.dnd.dpr.spells.Spell
import com.vikinghelmet.dnd.dpr.util.Globals

data class Scenario(
    val attacker: Combatant,
    val turns: List<Turn>,
    val numTargets: Int,
    val targetSpacing: Int,
)
{
    override fun equals(other: Any?): Boolean {
        if (other == null || other !is Scenario) return false
        if (this.attacker != other.attacker) return false
        if (this.numTargets != other.numTargets) return false
        if (this.targetSpacing != other.targetSpacing) return false
        return turns.size == other.turns.size && turns.containsAll(other.turns) && other.turns.containsAll(turns)
    }

    fun partialMatch(other: Scenario, turnsSoFar: Int): Boolean {
        if (this.attacker != other.attacker) return false
        if (this.numTargets != other.numTargets) return false
        if (this.targetSpacing != other.targetSpacing) return false

        for (i in 0..turnsSoFar) {
            if (turns[i] != other.turns[i]) return false
        }
        return true
    }

    fun getSpellsAcrossTurns(): List<Spell> {
        val result = ArrayList<Spell>()
        for (turn in turns) for (a in turn.attacks) {
            if (a.action is Spell) result.add(a.action)
        }
        return result;
    }

    fun isSlotAvailable(spell: Spell): Boolean {
        return Spell.isSlotAvailable(attacker, spell, getSpellsAcrossTurns())
    }

    fun getLabel(): String {
        val buf = StringBuilder()
        for (turn in turns) {
            val attackNameList = mutableListOf<String>()
            turn.attacks.map { attackNameList.add(it.action.toString()) }
            buf.append(""+attackNameList)
        }
        return Globals.wrapWithQuotes(buf.toString())
    }
}
