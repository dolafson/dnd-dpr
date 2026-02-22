package com.vikinghelmet.dnd.dpr.character

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class Character(
    @SerialName("data")
    val characterData: Data,
    val id: Int,
    val message: String,
    val success: Boolean
)
