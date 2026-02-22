package com.vikinghelmet.dnd.dpr.character

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class CharacterClass(
    val definition: ClassDefinition,
    val level: Int,
)