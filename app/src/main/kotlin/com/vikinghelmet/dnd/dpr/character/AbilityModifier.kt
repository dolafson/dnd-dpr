package com.vikinghelmet.dnd.dpr.character

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class AbilityModifier(
    val entityId: Int? = null,
    val subType: String,
    val type: String,
    val value: Int? = null
)