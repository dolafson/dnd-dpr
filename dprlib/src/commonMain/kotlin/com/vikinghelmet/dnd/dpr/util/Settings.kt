package com.vikinghelmet.dnd.dpr.util

import kotlinx.serialization.Serializable

@Serializable
data class Settings(
    val characterId: String? = null,
    val monsterName: String? = null,
    val targetProximity: Int? = 0
)
