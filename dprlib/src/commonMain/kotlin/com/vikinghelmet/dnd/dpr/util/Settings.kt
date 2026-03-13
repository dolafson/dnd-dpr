package com.vikinghelmet.dnd.dpr.util

import kotlinx.serialization.Serializable

@Serializable
data class Settings(
    var characterId: String? = null,
    var monsterName: String? = null,
    var proximity: Int? = 0
) {
    fun copy(other: Settings) {
        characterId = other.characterId
        monsterName = other.monsterName
        proximity = other.proximity
    }

}
