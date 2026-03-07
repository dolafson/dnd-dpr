package com.vikinghelmet.dnd.dpr.scenario

import com.vikinghelmet.dnd.dpr.character.inventory.Weapon
import com.vikinghelmet.dnd.dpr.spells.Spell

data class PotentialAction(val spell: Spell? = null, val weapon: Weapon? = null) {
    fun getName(): String {
        return if (spell != null) spell.name else if (weapon != null) weapon.name else ""
    }
}

data class ActionsAvailable(
    val meleeActionList: MutableList<PotentialAction> = mutableListOf(),
    val rangedActionList: MutableList<PotentialAction> = mutableListOf()
) {
    fun getFullList(isMelee: Boolean): List<PotentialAction> {
        return if (isMelee) meleeActionList else rangedActionList
    }

    fun getNameList(isMelee: Boolean): List<String> {
        val result = mutableListOf<String>()
        val alist = if (isMelee) meleeActionList else rangedActionList
        for (action in alist) result.add (action.getName())
        return result
    }

    fun add(range: Int, spell: Spell) {
        if (spell.triggersSavingThrow()) { // this type of spell is added to both lists ...
            meleeActionList.add(PotentialAction(spell,null))
            rangedActionList.add(PotentialAction(spell,null))
        }
        else {
            add(range, spell, null)
        }
    }

    fun add(range: Int, weapon: Weapon) {
        add(range, null, weapon)
    }

    fun add(range: Int, spell: Spell?, weapon: Weapon?) {
        if (range <= 5) {
            meleeActionList.add(PotentialAction(spell, weapon))
        }
        else {
            rangedActionList.add(PotentialAction (spell, weapon))
        }
    }
}