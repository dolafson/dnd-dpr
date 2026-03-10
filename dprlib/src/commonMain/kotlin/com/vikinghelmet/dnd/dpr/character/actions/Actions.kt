@file:OptIn(ExperimentalSerializationApi::class)

package com.vikinghelmet.dnd.dpr.character.actions

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class Actions(
    val race: List<com.vikinghelmet.dnd.dpr.character.actions.ActionAdded>,
    val feat: List<com.vikinghelmet.dnd.dpr.character.actions.ActionAdded>,
    @SerialName("class") val classActions: List<com.vikinghelmet.dnd.dpr.character.actions.ActionAdded>,
)