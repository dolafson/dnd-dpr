package com.vikinghelmet.dnd.dpr.character.actions

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class Dice(
    val diceCount: Int,
    //val diceMultiplier: Any,
    val diceString: String,
    val diceValue: Int,
    //val fixedValue: Any
)