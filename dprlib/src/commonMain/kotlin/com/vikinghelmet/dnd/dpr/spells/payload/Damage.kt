package com.vikinghelmet.dnd.dpr.spells.payload

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Damage")
data class Damage(
    val ability: String,
    val damageType: String,
    val _bonus: Int? = null,
    val diceCount: Int? = null,
    val diceSize: String? = null
) : Payload()