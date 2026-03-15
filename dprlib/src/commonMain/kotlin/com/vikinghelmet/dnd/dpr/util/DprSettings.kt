package com.vikinghelmet.dnd.dpr.util

import kotlinx.serialization.Serializable

@Serializable
data class CharacterListItem(
    var remoteId: String,
    var localId: String,
    var name: String,
) {
    fun copyValues(other: CharacterListItem) {
        remoteId = other.remoteId
        localId = other.localId
        name = other.name
    }

}

@Serializable
data class DprSettings(
    var characterName: String = "",
    var monsterName: String = "",
    var proximity: Int = 0,
    var characterList: MutableList<CharacterListItem> = mutableListOf(),
) {
}
