package com.vikinghelmet.dnd.dpr.util

import kotlinx.serialization.Serializable

@Serializable
data class DprSettings(
    var characterName: String = "",
    var monsterName: String = "",
    var proximity: Int = 0
) {
}
