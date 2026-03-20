package com.vikinghelmet.dnd.dpr.scenario

import com.vikinghelmet.dnd.dpr.character.Character
import com.vikinghelmet.dnd.dpr.spells.Spell
import com.vikinghelmet.dnd.dpr.spells.SpellsWithComplexRules.HuntersMark
import com.vikinghelmet.dnd.dpr.turn.Turn
import com.vikinghelmet.dnd.dpr.util.Constants.levelToFavoredEnemyMap
import com.vikinghelmet.dnd.dpr.util.Globals

data class Scenario(
    val character: Character,
    val turns: List<Turn>
)
{
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

        if (spell.name == HuntersMark.getNameWithWS()) {
            val maxSlots = levelToFavoredEnemyMap[character.getLevel()] ?: return false
            val slotsUsed = getSpellsAcrossTurns().count { it.name == spell.name }
            return (slotsUsed < maxSlots)
        }

        val maxSlots = character.getSpellSlots()[level - 1]
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
