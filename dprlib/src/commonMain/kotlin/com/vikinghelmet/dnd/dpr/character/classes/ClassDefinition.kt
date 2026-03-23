@file:OptIn(ExperimentalSerializationApi::class)

package com.vikinghelmet.dnd.dpr.character.classes

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class ClassDefinition(
    val id: Int,
    val name: String,
    val spellCastingAbilityId: Int? = null,
    val spellRules: com.vikinghelmet.dnd.dpr.character.classes.SpellRules? = null,
    val classFeatures: MutableList<ClassFeature> = mutableListOf(),
    /*
data.classes.0.definition.spellRules.levelSpellSlots.9.0 = 4
data.classes.0.definition.spellRules.levelSpellSlots.9.1 = 3
data.classes.0.definition.spellRules.levelSpellSlots.9.2 = 2

     */
)