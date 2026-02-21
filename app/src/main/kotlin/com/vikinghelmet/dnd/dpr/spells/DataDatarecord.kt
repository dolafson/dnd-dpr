package com.vikinghelmet.dnd.dpr.spells

import com.vikinghelmet.dnd.dpr.spells.payload.Payload
import kotlinx.serialization.Serializable

@Serializable
data class DataDatarecord(
    val name: String,
    val level: String? = null,
    val parent: String? = null,
    val payload: Payload
)