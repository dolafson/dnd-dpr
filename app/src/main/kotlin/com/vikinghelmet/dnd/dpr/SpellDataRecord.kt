package com.vikinghelmet.dnd.dpr
import kotlinx.serialization.Serializable

@Serializable
data class SpellDataRecord(
    val name: String,
    val level: Int? = null,
    val parent: String? = null,
    val payload: String,
)
