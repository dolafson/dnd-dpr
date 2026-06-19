package com.vikinghelmet.dnd.dpr.util

import com.vikinghelmet.dnd.dpr.action.CombatantMenuItem
import com.vikinghelmet.dnd.dpr.editable.EditablePlayerCharacter
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Party(
    var partyName: String,
    var remoteList: List<String>,
    var localList: MutableList<String> = mutableListOf(),
) : CombatantMenuItem {
    @Transient
    val characterList = mutableListOf<EditablePlayerCharacter>()

    fun add(character: EditablePlayerCharacter) {
        characterList.add(character)
        localList.add(character.getName())
    }

    override fun getName() = partyName
    override fun toString() = partyName
}