package com.vikinghelmet.dnd.dpr.monsters

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Action(
    val Name: String,
    val Desc: String? = null,

    val Damage: String? = null,
    @SerialName("Damage Type")
    val DamageType: String? = null,

    @SerialName("Damage 2")
    val Damage2: String? = null,
    @SerialName("Damage 2 Type")
    val Damage2Type: String? = null,

    @SerialName("Hit Bonus")
    val HitBonus: String? = null,

    val Reach: String? = null,
    val Target: String? = null,
    val Type: String? = null,

    @SerialName("Type Attack")
    val TypeAttack: String? = null
)