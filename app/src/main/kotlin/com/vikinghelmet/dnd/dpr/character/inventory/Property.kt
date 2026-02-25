@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.vikinghelmet.dnd.dpr.character.inventory

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class Property(
    //val description: String,
    //val id: Int,
    val name: String,

    // notes = "1d8" ...
    // A Versatile weapon can be used with one or two hands. A damage value in parentheses appears with the property.
    // The weapon deals that damage when used with two hands to make a melee attack
    // val notes: String? = null
)