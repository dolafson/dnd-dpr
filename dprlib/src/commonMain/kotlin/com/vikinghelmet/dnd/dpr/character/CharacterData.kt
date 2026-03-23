@file:OptIn(ExperimentalSerializationApi::class)

package com.vikinghelmet.dnd.dpr.character

import com.vikinghelmet.dnd.dpr.character.actions.Actions
import com.vikinghelmet.dnd.dpr.character.background.Background
import com.vikinghelmet.dnd.dpr.character.campaign.Campaign
import com.vikinghelmet.dnd.dpr.character.classes.CharacterClass
import com.vikinghelmet.dnd.dpr.character.feats.FeatAdded
import com.vikinghelmet.dnd.dpr.character.inventory.InventoryItem
import com.vikinghelmet.dnd.dpr.character.modifiers.Modifiers
import com.vikinghelmet.dnd.dpr.character.race.Race
import com.vikinghelmet.dnd.dpr.character.spells.ClassSpell
import com.vikinghelmet.dnd.dpr.character.spells.SpellGroup
import com.vikinghelmet.dnd.dpr.character.stats.Stat
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class CharacterData(
    val username: String,
    val id: Int? = 0,
    val name: String,
    val background: Background? = null,
    val campaign: Campaign? = null,
    val characterValues: List<CharacterValues>? = null,
    val classes: List<CharacterClass>,
    val inventory: ArrayList<InventoryItem>? = null,
    val modifiers: Modifiers,
    val actions: Actions,
    val stats: List<Stat>,
    val feats: ArrayList<FeatAdded>,
    val race: Race,
    val classSpells: List<ClassSpell>?= emptyList(),
    val spells: SpellGroup
)