package com.vikinghelmet.dnd.dpr.character.background

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class Background(
    // stuff we need
    val definition: BackgroundDefinition,
)


@JsonIgnoreUnknownKeys
@Serializable
data class BackgroundDefinition(
    // stuff we need
    val id: Int,
)
