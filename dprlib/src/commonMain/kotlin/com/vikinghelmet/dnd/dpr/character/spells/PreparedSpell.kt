package com.vikinghelmet.dnd.dpr.character.spells

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class PreparedSpell(
    val definition: com.vikinghelmet.dnd.dpr.character.spells.Definition,
    /*
    val activation: Activation,
    val additionalDescription: String,
    val alwaysPrepared: Boolean,
    val atWillLimitedUseLevel: Any,
    val baseLevelAtWill: Boolean,
    val castAtLevel: Any,
    val castOnlyAsRitual: Boolean,
    val componentId: Int,
    val componentTypeId: Int,
    val countsAsKnownSpell: Boolean,
    val definitionId: Int,
    val displayAsAttack: Any,
    val entityTypeId: Int,
    val id: Int,
    val isSignatureSpell: Any,
    val limitedUse: Any,
    val overrideSaveDc: Any,
    val prepared: Boolean,
    val range: RangeX,
    val restriction: String,
    val ritualCastingType: Any,
    val spellCastingAbilityId: Int,
    val spellListId: Any,
    val usesSpellSlot: Boolean

     */
)