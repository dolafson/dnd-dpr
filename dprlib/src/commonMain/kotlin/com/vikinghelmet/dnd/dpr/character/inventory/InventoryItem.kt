@file:OptIn(ExperimentalSerializationApi::class)

package com.vikinghelmet.dnd.dpr.character.inventory

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class InventoryItem(
    val definition: com.vikinghelmet.dnd.dpr.character.inventory.Definition,
    val equipped: Boolean? = false,
    val id: Int,
    //val quantity: Int
)