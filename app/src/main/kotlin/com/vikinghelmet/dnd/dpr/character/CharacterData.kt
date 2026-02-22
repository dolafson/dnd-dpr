@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.vikinghelmet.dnd.dpr.character

import com.vikinghelmet.dnd.dpr.character.abilities.AbilityModifiers
import com.vikinghelmet.dnd.dpr.character.abilities.AbilityScore
import com.vikinghelmet.dnd.dpr.character.feats.FeatAdded
import com.vikinghelmet.dnd.dpr.character.race.Race
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class CharacterData(
    val classes: List<CharacterClass>,
    val modifiers: AbilityModifiers,
    val stats: List<AbilityScore>,
    val feats: List<FeatAdded>,
    val race: Race,
)