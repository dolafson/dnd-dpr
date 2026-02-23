@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.vikinghelmet.dnd.dpr.character

import com.vikinghelmet.dnd.dpr.character.classes.CharacterClass
import com.vikinghelmet.dnd.dpr.character.feats.FeatAdded
import com.vikinghelmet.dnd.dpr.character.modifiers.Modifiers
import com.vikinghelmet.dnd.dpr.character.race.Race
import com.vikinghelmet.dnd.dpr.character.stats.Stat
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class CharacterData(
    val username: String,
    val name: String,
    val characterValues: List<CharacterValues>? = null,
    val classes: List<CharacterClass>,
    val modifiers: Modifiers,
    val stats: List<Stat>,
    val feats: List<FeatAdded>,
    val race: Race,
)