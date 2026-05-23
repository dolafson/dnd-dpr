package com.vikinghelmet.dnd.dpr.character.inventory

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class PCInventoryItemDamage(
    //val diceCount: Int,
    val diceString: String,
    //val diceValue: Int,
)