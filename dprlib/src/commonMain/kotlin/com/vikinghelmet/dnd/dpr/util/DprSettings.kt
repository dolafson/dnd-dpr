package com.vikinghelmet.dnd.dpr.util

import kotlinx.serialization.Serializable

@Serializable
data class CharacterListItem(
    var remoteId: String,
    var name: String,
    var isLocal: Boolean, // copy of remote, with local modifications (lookup by name)
) {
    fun copyValues(other: CharacterListItem) {
        remoteId = other.remoteId
        name = other.name
        isLocal = other.isLocal
    }

}

@Serializable
data class DprSettings(
    var characterName: String = "",
    var monsterName: String = "",
    var proximity: Int = 0
) {
}
