package com.vikinghelmet.dnd.dpr.scenario.onesided

import com.vikinghelmet.dnd.dpr.action.Combatant
import com.vikinghelmet.dnd.dpr.action.Turn
import com.vikinghelmet.dnd.dpr.character.PlayerCharacter
import com.vikinghelmet.dnd.dpr.spells.Spell
import com.vikinghelmet.dnd.dpr.spells.SpellsWithComplexRules.HuntersMark
import com.vikinghelmet.dnd.dpr.util.Constants.levelToFavoredEnemyMap
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

    fun getSpellsAcrossTurns(): List<Spell> {
        val result = ArrayList<Spell>()
        for (turn in turns) for (a in turn.attacks) {
            if (a.action is Spell) result.add(a.action)
        }
        return result;
    }

    fun isSlotAvailable(spell: Spell): Boolean {
        val level = spell.properties.Level
        if (level == 0) return true // cantrip

        if (attacker is PlayerCharacter && spell.name == HuntersMark.getNameWithWS()) {
            val maxSlots = levelToFavoredEnemyMap[attacker.getLevel()] ?: return false
            val slotsUsed = getSpellsAcrossTurns().count { it.name == spell.name }
            return (slotsUsed < maxSlots)
        }

        val maxSlots = attacker.getSpellSlots()[level - 1]
        val slotsUsed = getSpellsAcrossTurns().count { it.properties.Level == spell.properties.Level && it.name != HuntersMark.getNameWithWS() }
        if (slotsUsed >= maxSlots) Globals.  debug("not enough slots: level=$level, slotsUsed=$slotsUsed, max=$maxSlots, spellsUsed = "+getSpellsAcrossTurns())
        return (slotsUsed < maxSlots)
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
