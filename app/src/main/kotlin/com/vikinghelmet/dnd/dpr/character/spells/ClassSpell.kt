package com.vikinghelmet.dnd.dpr.character.spells

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class ClassSpell(
    val spells: List<PreparedSpell>
)