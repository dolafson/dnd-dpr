package com.vikinghelmet.dnd.dpr.character.spells

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class AlwaysPreparedSpells(
    val data: List<PreparedSpellRemote> = mutableListOf(),
)