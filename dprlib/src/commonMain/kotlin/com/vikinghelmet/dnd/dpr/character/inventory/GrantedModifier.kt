package com.vikinghelmet.dnd.dpr.character.inventory

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class Dice(
    val diceCount: Int,
    val diceString: String,
    val diceValue: Int,
)

@JsonIgnoreUnknownKeys
@Serializable
data class GrantedModifier(
    val type: String,
    val subType: String,
    val value: Int? = null,

    val componentTypeId: Int,
    val modifierSubTypeId: Int,
    val modifierTypeId: Int,
    val restriction: String,

    val dice: Dice? = null, // TODO: flaming spear, etc
)