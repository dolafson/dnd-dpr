package com.vikinghelmet.dnd.dpr.spells.payload

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Upcasting")
data class Upcasting(
    val mode: String,
    val startingLevel: Int? = null,
    val target: String,
    val changeMode: String,
    val level: String,
    val value: String,
) : Payload()