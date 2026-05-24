package com.vikinghelmet.dnd.dpr.monsters

import kotlinx.serialization.Serializable

@Serializable
data class ProficiencyX(
    val index: String,
    val name: String,
    val url: String
)

@Serializable
data class Proficiency(
    val proficiency: ProficiencyX,
    val value: Int
)