package com.vikinghelmet.dnd.dpr.character.race

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class Race(
    val baseRaceName: String,
    val fullName: String,
    val racialTraits: List<com.vikinghelmet.dnd.dpr.character.race.RacialTraitAdded>
)
