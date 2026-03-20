@file:OptIn(ExperimentalSerializationApi::class)

package com.vikinghelmet.dnd.dpr.character.classes

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class CharacterClass(
    val definition: com.vikinghelmet.dnd.dpr.character.classes.ClassDefinition,
    val subclassDefinition: com.vikinghelmet.dnd.dpr.character.classes.ClassDefinition? = null,
    val level: Int,
)