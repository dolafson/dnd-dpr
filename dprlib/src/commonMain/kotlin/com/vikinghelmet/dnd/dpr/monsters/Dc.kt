package com.vikinghelmet.dnd.dpr.monsters

import kotlinx.serialization.Serializable

@Serializable
data class DcType(
    val index: String,
    val name: String,
    val url: String
)

@Serializable
data class Dc(
    val dc_type: DcType,
    val dc_value: Int,
    val success_type: String
)