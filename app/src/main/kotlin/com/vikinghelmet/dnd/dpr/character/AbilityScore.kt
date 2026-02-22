package com.vikinghelmet.dnd.dpr.character

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class AbilityScore(
    val id: Int,
    val name: String? = null,
    val value: Int
)