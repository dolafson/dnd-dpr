package com.vikinghelmet.dnd.dpr.monsters

import kotlinx.serialization.Serializable

@Serializable
data class Usage(
    val dice: String ?= null,
    val min_value: Int ?= 0,
    val type: String,
    val times: Int ?= 0,
    val rest_types: List<String> = emptyList(),
)