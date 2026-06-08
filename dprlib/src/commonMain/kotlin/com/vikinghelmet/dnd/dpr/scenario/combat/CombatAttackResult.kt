package com.vikinghelmet.dnd.dpr.scenario.combat

import com.vikinghelmet.dnd.dpr.action.Attack
import dev.shivathapaa.logger.api.LoggerFactory
import kotlinx.serialization.Transient

data class CombatAttackResult(
    val combatant: CombatantWithStatus,
    val targetList: List<CombatantWithStatus>,

    val totalDamage: Int,

    var attack: Attack,
    var turnId: Int = -1,
    var actionId: Int = -1,
    var effectId: Int = -1,
) {
    @Transient private val logger = LoggerFactory.get(CombatAttackResult::class.simpleName ?: "")

    override fun toString(): String {
        return "$combatant -> $targetList: totalDamage=$totalDamage, attack=${attack.action.getActionName() })"
    }
}

