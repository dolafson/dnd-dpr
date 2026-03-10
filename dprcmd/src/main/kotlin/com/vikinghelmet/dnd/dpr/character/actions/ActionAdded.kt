package com.vikinghelmet.dnd.dpr.character.actions

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class ActionAdded(
    val name: String,
    val snippet: String,
    val limitedUse: LimitedUse? = null,
    val activation: Activation? = null,
    val dice: Dice? = null,
    val range: Range? = null,

    /*
    val abilityModifierStatId: Any,
    val actionType: Int,
    val ammunition: Any,
    val attackSubtype: Any,
    val attackTypeRange: Any,
    val componentId: Int,
    val componentTypeId: Int,
    val damageTypeId: Int,
    val description: String,
    val displayAsAttack: Boolean,
    val entityTypeId: String,
    val fixedSaveDc: Any,
    val fixedToHit: Any,
    val id: String,
    val isMartialArts: Boolean,
    val isProficient: Boolean,

    val numberOfTargets: Any,
    val onMissDescription: String,
    val saveFailDescription: String,
    val saveStatId: Any,
    val saveSuccessDescription: String,
    val spellRangeType: Any,
    val value: Any
 */
)