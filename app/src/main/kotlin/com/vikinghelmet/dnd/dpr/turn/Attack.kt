package com.vikinghelmet.dnd.dpr.turn

import kotlinx.serialization.Serializable

@Serializable
data class Attack(
    // required fields
    val monster: String,
    val attack: String, // name of spell or weapon

    // optional fields
    val preconditions: Preconditions? = null,
    val isBonusAction: Boolean? = false,
    val notes: List<String>? = null,
    val numTargets: Int? = 1
)