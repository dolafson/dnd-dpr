package com.vikinghelmet.dnd.dpr.character

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class AbilityModifiers(
    val race: List<AbilityModifier>
)