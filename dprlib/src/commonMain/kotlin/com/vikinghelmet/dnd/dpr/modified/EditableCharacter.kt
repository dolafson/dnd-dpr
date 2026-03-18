package com.vikinghelmet.dnd.dpr.modified

import com.vikinghelmet.dnd.dpr.character.Character
import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class EditableCharacter (
    val from: Character,
    val editableFields: EditableFields
) : Character(from.characterData, from.id, from.message, from.success)
{
    override fun getModifiedAbilityScore(a: AbilityType): Int {
        return editableFields.stats[a] ?: 0
    }

    override fun getLevel(): Int {
        return editableFields.level
    }

    override fun getName(): String {
        return editableFields.name
    }

}