package com.vikinghelmet.dnd.dpr.character

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class Data(
    val classes: List<CharacterClass>,
    val modifiers: AbilityModifiers,
    val stats: List<AbilityScore>,
    val race: Race,
)