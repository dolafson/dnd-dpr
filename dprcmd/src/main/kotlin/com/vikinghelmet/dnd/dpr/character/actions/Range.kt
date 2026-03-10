package com.vikinghelmet.dnd.dpr.character.actions

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class Range(
    //val aoeSize: Any,
    //val aoeType: Any,
    val hasAoeSpecialDescription: Boolean? = false,
    //val longRange: Any,
    //val minimumRange: Any,
    val range: Int? = 0
)