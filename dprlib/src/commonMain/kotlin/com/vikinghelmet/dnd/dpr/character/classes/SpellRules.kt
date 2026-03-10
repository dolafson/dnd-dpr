package com.vikinghelmet.dnd.dpr.character.classes

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
class SpellRules {
    val levelSpellSlots: ArrayList<ArrayList<Int>> = ArrayList()
}