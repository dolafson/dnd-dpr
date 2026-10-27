package com.vikinghelmet.dnd.dpr.scenario

import com.vikinghelmet.dnd.dpr.action.Action
import com.vikinghelmet.dnd.dpr.action.Weapon
import com.vikinghelmet.dnd.dpr.action.enums.WeaponProperty
import com.vikinghelmet.dnd.dpr.spells.Spell
import com.vikinghelmet.dnd.dpr.util.Constants

class ActionsAvailable { // container for PRIMARY combat actions: weapons or non-BA-non-ritual spells
    val mapOfLists = mutableMapOf<Int, MutableList<Action>>()

    override fun toString(): String {
        return "$mapOfLists"
    }

    fun getRanges(): List<Int> {return mapOfLists.keys.toList() }

    // filter out superficial duplicates (ie, shortsword1, shortsword2)
    // to avoid wasting cpu on redundant scenarios
    fun getLightWeaponsForBA(targetProximity: Int, exclude: Weapon): List<Action> {
        return getListWithPotentialDups(targetProximity).filter {
            it is Weapon &&  it.hasWeaponProperty(WeaponProperty.Light)  &&  it !== exclude // previously it != exclude
        }.distinctBy { it.getActionName() }
    }

    fun getPrimaryAction(targetProximity: Int): List<Action> {
        return getListWithPotentialDups(targetProximity).distinctBy { it.getActionName() }
    }

    private fun getListWithPotentialDups(targetProximity: Int): List<Action> {
        val result = mutableListOf<Action>()

        for (key in mapOfLists.keys) {
            if (targetProximity <= Constants.MELEE_RANGE) {
                if (key <= Constants.MELEE_RANGE) result.addAll(mapOfLists[key]!!)
            }
            else {
                if (Constants.MELEE_RANGE < targetProximity && targetProximity <= key) result.addAll(mapOfLists[key]!!)
            }
        }
        return result
    }

    fun add(range: Int, action: Action) {
        mapOfLists.getOrPut(range) { mutableListOf() }.add(action)
    }

    fun addSpell(spell: Spell) {
        if (spell.isRangedSpellAttack()) {
            add(spell.getRange(), spell)
        }
        else {
            // all spells - except for "ranged spell attack" - can be used in melee
            add(Constants.MELEE_RANGE, spell)

            if (spell.getRange() > Constants.MELEE_RANGE) {
                add(spell.getRange(), spell)
            }
            else {
                // "range = self, but aoe is 60 ft cone ..."
                // this may be inaccurate for some AOE shapes, but close enough for now
                if (spell.getSpellAttacks(0).isNotEmpty()) {
                    val aoeSize = spell.getSpellAttacks(0).first().getAoeSize()
                    if (aoeSize > 0) {
                        add(aoeSize, spell)
                    }
                }
            }
        }
    }

    fun isEmpty() = mapOfLists.isEmpty()
}