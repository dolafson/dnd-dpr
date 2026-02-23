@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.vikinghelmet.dnd.dpr.character

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class CharacterValues(
    val notes: String? = null,
)