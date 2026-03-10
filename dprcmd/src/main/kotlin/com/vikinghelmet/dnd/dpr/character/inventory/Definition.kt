@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.vikinghelmet.dnd.dpr.character.inventory

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class Definition(
    val name: String,
    val filterType: String,
    val magic: Boolean,

    // val type: String,

    val armorClass: Int? = null,
    val attackType: Int? = null,

    val damage: Damage? = null,
    val damageType: String? = null,
    val properties: List<Property>? = null,

    val range: Int? = null,
    val longRange: Int? = null,
)