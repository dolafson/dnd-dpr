package com.vikinghelmet.dnd.dpr.action

import com.vikinghelmet.dnd.dpr.scenario.Scenario

interface MeleeOrRangeAction {
    fun getDamageList(): List<Damage>
    fun getAttackBonus(): Int
    fun getNumTargetsAffected(scenario: Scenario): Int
}