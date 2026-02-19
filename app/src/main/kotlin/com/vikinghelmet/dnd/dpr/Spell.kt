package com.vikinghelmet.dnd.dpr
import kotlinx.serialization.Serializable

@Serializable
data class Spell(
    val name: String,
    val description: String,
    val publisher: String,
    val book: String,
    val properties: SpellProperties
)
