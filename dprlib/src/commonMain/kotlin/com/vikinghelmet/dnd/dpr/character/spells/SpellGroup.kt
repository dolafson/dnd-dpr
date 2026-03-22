package com.vikinghelmet.dnd.dpr.character.spells

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class SpellGroup(
    @SerialName("class") val classSpells: List<com.vikinghelmet.dnd.dpr.character.spells.PreparedSpellRemote>,
    @SerialName("race") val raceSpells: List<com.vikinghelmet.dnd.dpr.character.spells.PreparedSpellRemote>
)