@file:OptIn(ExperimentalSerializationApi::class)

package com.vikinghelmet.dnd.dpr.character.modifiers

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class Modifiers(
    val race: List<com.vikinghelmet.dnd.dpr.character.modifiers.Modifier>,
    val feat: List<com.vikinghelmet.dnd.dpr.character.modifiers.Modifier>,
    @SerialName("class") val classMod: List<com.vikinghelmet.dnd.dpr.character.modifiers.Modifier>,
)