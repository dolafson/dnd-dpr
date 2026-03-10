@file:OptIn(ExperimentalSerializationApi::class)

package com.vikinghelmet.dnd.dpr.character.stats

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class Stat(
    val id: Int,
    val name: String? = null,
    var value: Int // for quick what-if assessments, it's useful for this to be editable
)