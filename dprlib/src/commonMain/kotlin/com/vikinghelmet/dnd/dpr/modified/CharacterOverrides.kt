@file:OptIn(ExperimentalSerializationApi::class)

package com.vikinghelmet.dnd.dpr.modified

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class CharacterOverrides (
    var localId: String, // populate this with a UUID
    var remoteId: Int, // not technically an override, but a cross-reference to original character from dndbeyond
    var level: Int,
    var name: String,
    var stats: StatBlock,
)
