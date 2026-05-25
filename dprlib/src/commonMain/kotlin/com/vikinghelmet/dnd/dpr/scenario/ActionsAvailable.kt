package com.vikinghelmet.dnd.dpr.scenario

import com.vikinghelmet.dnd.dpr.action.Action
import com.vikinghelmet.dnd.dpr.action.Weapon
import com.vikinghelmet.dnd.dpr.action.enums.WeaponProperty
import com.vikinghelmet.dnd.dpr.util.Constants

class ActionsAvailable {
    val mapOfLists = mutableMapOf<Int, MutableList<Action>>()

    override fun toString(): String {
        return "$mapOfLists"
    }

    fun getRanges(): List<Int> {return mapOfLists.keys.toList() }

    // filter out superficial duplicates (ie, shortsword1, shortsword2)
    // to avoid wasting cpu on redundant scenarios
    fun getLightWeaponsForBA(targetProximity: Int, exclude: Weapon): List<Action> {
        return getListWithPotentialDups(targetProximity).filter {
            it is Weapon  &&  it.hasWeaponProperty(WeaponProperty.Light)  &&  it != exclude
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
}