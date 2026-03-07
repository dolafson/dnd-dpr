package com.vikinghelmet.dnd.dpr.scenario

import com.vikinghelmet.dnd.dpr.character.Character
import com.vikinghelmet.dnd.dpr.spells.Spell
import com.vikinghelmet.dnd.dpr.turn.Turn

data class Scenario(
    val character: Character,
    val turns: List<Turn>
)
{
    fun getSpellsAcrossTurns(): List<Spell> {
        val result = ArrayList<Spell>()
        for (turn in turns) for (a in turn.attacks) {
            if (a.attack is Spell) result.add(a.attack)
        }
        return result;
    }

    fun isSlotAvailable(spell: Spell): Boolean {
        val level = spell.properties.Level
        if (level == 0) return true // cantrip

        val maxSlots = character.getSpellSlots()[level - 1]
        val slotsUsed = getSpellsAcrossTurns().count { it.properties.Level == spell.properties.Level }
        return (slotsUsed < maxSlots)
    }

    fun getLabel(): String {
        val buf = StringBuilder()
        for (turn in turns) {
            val attackNameList = mutableListOf<String>()
            turn.attacks.map { attackNameList.add(it.attack.toString()) }
            buf.append(""+attackNameList)
        }
        return String.format("\"%s\"", buf.toString())
    }
}