package com.vikinghelmet.dnd.dpr.character.actions

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class ActionAdded(
    val name: String,
    val description: String? = null,
    val snippet: String,

    val saveStatId: Int? = null,
    val saveFailDescription: String? = null,
    val saveSuccessDescription: String? = null,

    val limitedUse: com.vikinghelmet.dnd.dpr.character.actions.LimitedUse? = null,
    val activation: com.vikinghelmet.dnd.dpr.character.actions.Activation? = null,
    val dice: com.vikinghelmet.dnd.dpr.character.actions.Dice? = null,
    val range: com.vikinghelmet.dnd.dpr.character.actions.Range? = null,

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