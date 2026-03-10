package com.vikinghelmet.dnd.dpr.character.spells

import kotlinx.serialization.Serializable

@Serializable
data class Range(
    val aoeType: String? = null,
    val aoeValue: Int ?= 0,
    val origin: String ?= null,
    val rangeValue: Int ?= 0
)