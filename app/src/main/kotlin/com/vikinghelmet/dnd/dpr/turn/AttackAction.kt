package com.vikinghelmet.dnd.dpr.turn

import com.vikinghelmet.dnd.dpr.character.inventory.Weapon
import com.vikinghelmet.dnd.dpr.spells.Spell

data class AttackAction(val spell: Spell? = null, val weapon: Weapon? = null) {
    fun getName(): String {
        return if (spell != null) spell.name else if (weapon != null) weapon.name else ""
    }
}

data class ActionsAvailable(
    val meleeActionList: MutableList<AttackAction> = mutableListOf(),
    val rangedActionList: MutableList<AttackAction> = mutableListOf()
) {
    fun getFullList(isMelee: Boolean): List<AttackAction> {
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
            meleeActionList.add(AttackAction(spell,null))
            rangedActionList.add(AttackAction(spell,null))
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
            meleeActionList.add(AttackAction(spell, weapon))
        }
        else {
            rangedActionList.add(AttackAction (spell, weapon))
        }
    }
}