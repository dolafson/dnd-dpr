package com.vikinghelmet.dnd.dpr.scenario

import com.vikinghelmet.dnd.dpr.turn.AttackAction
import com.vikinghelmet.dnd.dpr.util.Constants

class ActionsAvailable {
    val mapOfLists = mutableMapOf<Int, MutableList<AttackAction>>()

    fun getRanges(): List<Int> {return mapOfLists.keys.toList() }

    fun getList(targetProximity: Int): List<AttackAction> {
        val result = mutableListOf<AttackAction>()

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

    fun add(range: Int, attackAction: AttackAction) {
        mapOfLists.getOrPut(range) { mutableListOf() }.add(attackAction)
    }
}