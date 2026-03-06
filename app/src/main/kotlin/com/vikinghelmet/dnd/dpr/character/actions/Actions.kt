@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.vikinghelmet.dnd.dpr.character.actions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class Actions(
    val race: List<ActionAdded>,
    val feat: List<ActionAdded>,
    @SerialName("class") val classActions: List<ActionAdded>,
)