package com.vikinghelmet.dnd.dpr.character

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class RacialTrait(
    val definition: RacialTraitDefinition,
) {
    @JsonIgnoreUnknownKeys
    @Serializable
    data class RacialTraitDefinition(
        val id: Int,
        val definitionKey: String,
        val name: String,
        val description: String,
        val snippet: String,
    )
}