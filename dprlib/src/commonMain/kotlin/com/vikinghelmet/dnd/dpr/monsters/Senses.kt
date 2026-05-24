package com.vikinghelmet.dnd.dpr.monsters

import kotlinx.serialization.Serializable

@Serializable
data class Senses(
    val blindsight: String ?= null,
    val darkvision: String ?= null,
    val passive_perception: Int,
    val tremorsense: String ?= null,
    val truesight: String ?= null,
)