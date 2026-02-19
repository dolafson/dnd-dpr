package com.vikinghelmet.dnd.dpr
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@Serializable
// @JsonIgnoreUnknownKeys
data class DataRecord(
    val name: String,
    val level: Int? = null,
    val parent: String? = null,
    val payload: String,
)
