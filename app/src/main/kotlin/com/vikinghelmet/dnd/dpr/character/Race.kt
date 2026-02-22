package com.vikinghelmet.dnd.dpr.character

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class Race(
    val baseRaceName: String,
    val fullName: String,
    val racialTraits: List<RacialTrait>
)
