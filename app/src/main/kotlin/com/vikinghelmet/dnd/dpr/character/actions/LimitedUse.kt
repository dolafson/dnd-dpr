package com.vikinghelmet.dnd.dpr.character.actions

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class LimitedUse(
    val maxNumberConsumed: Int,
    val maxUses: Int,
    val minNumberConsumed: Int,
    val name: String? = null,
    val numberUsed: Int,
    val `operator`: Int,
    val proficiencyBonusOperator: Int,
    //val resetDice: Any,
    val resetType: Int,
    val statModifierUsesId: Int? = 0,
    val useProficiencyBonus: Boolean
)