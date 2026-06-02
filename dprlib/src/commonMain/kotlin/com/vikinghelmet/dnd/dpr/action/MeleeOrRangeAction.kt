package com.vikinghelmet.dnd.dpr.action

interface MeleeOrRangeAction {
    fun getDamageList(): List<Damage>
    fun getAttackBonus(): Int
    fun getNumTargetsAffected(numTargets: Int, targetSpacing: Int): Int
}