@file:OptIn(ExperimentalSerializationApi::class)

package com.vikinghelmet.dnd.dpr.editable

import com.vikinghelmet.dnd.dpr.character.Character
import com.vikinghelmet.dnd.dpr.util.NumericRange
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class EditableFields (
    var remoteId: Int, // cross-reference to original character from dndbeyond
    var level: Int,
    var name: String,
    var plan: MutableMap<String,PlanLevel> = mutableMapOf(),
){
    fun toPrettyPlan(): String {
        val buf = StringBuilder()
        for ((key, value) in plan) { buf.append("$key=$value").append("\n") }
        return "[$buf]"
    }

    companion object {
        fun fromCharacter(character: Character): EditableFields {
            return EditableFields(character.characterData.id!!, character.getLevel(), character.getName())
        }

        fun fromScreen(name: String, character: EditableCharacter, characterLevel: NumericRange): EditableFields
        {
            val result = fromCharacter(character)
            result.level = characterLevel.current
            result.name = name

            // use json serialization to get a deep copy
            result.plan = Json.decodeFromString (Json.encodeToString (character.editableFields.plan))
            return result
        }
    }
}
