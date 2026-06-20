package com.vikinghelmet.dnd.dpr.scenario.combat.results

import com.vikinghelmet.dnd.dpr.action.enums.DamageType
import kotlinx.serialization.Serializable

@Serializable
data class DamageResult(var amount: Int, val type: DamageType) {
    override fun toString() = "($amount, $type)"
}