@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.vikinghelmet.dnd.dpr.character.inventory

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class InventoryItem(
    val definition: Definition,
    val equipped: Boolean? = false,
    //val id: Int,
    //val quantity: Int
)