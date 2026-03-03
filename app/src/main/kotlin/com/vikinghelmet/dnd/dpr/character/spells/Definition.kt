package com.vikinghelmet.dnd.dpr.character.spells

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class Definition(
    val name: String,
    val range: Range,
    val concentration: Boolean,
    val tags: List<String>,
/*
    val activation: Activation,
    val asPartOfWeaponAttack: Boolean,
    val atHigherLevels: AtHigherLevelsX,
    val attackType: Any,
    val canCastAtHigherLevel: Boolean,
    val castingTimeDescription: String,
    val components: List<Int>,
    val componentsDescription: String,
    val conditions: List<Any?>,
    val damageEffect: Any,
    val definitionKey: String,
    val description: String,
    val duration: Duration,
    val healing: Any,
    val healingDice: List<Any?>,
    val id: Int,
    val isHomebrew: Boolean,
    val isLegacy: Boolean,
    val level: Int,
    val modifiers: List<ModifierX>,
    val rangeArea: Any,
    val requiresAttackRoll: Boolean,
    val requiresSavingThrow: Boolean,
    val ritual: Boolean,
    val saveDcAbilityId: Int,
    val scaleType: String,
    val school: String,
    val snippet: String,
    val sourceId: Any,
    val sourcePageNumber: Int,
    val sources: List<Source>,
    val spellGroups: List<Any?>,
    val tempHpDice: List<Any?>,
    val version: Any
 */
)