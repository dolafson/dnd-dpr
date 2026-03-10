@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.vikinghelmet.dnd.dpr.character.classes

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class ClassDefinition(
    val spellCastingAbilityId: Int? = null,
    val spellRules: SpellRules? = null,

    /*
data.classes.0.definition.spellRules.levelSpellSlots.9.0 = 4
data.classes.0.definition.spellRules.levelSpellSlots.9.1 = 3
data.classes.0.definition.spellRules.levelSpellSlots.9.2 = 2

     */
)