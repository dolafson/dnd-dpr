@file:OptIn(ExperimentalSerializationApi::class)

package com.vikinghelmet.dnd.dpr.editable

import com.vikinghelmet.dnd.dpr.character.Character
import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import com.vikinghelmet.dnd.dpr.util.EditableAbilityMap
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
    var stats: MutableMap<AbilityType, Int> = mutableMapOf(),
    //var plan: List<LevelPlan> = mutableListOf(),
    var plan: MutableMap<String,LevelPlan> = mutableMapOf(),
){
    companion object {
        fun fromCharacter(character: Character): EditableFields {
            val remoteId = character.characterData.id!!
            println("EditableFields: fromCharacter: remoteId = $remoteId")
            val result = EditableFields(remoteId, character.getLevel(), character.getName())

            AbilityType.entries.forEach {
                result.stats.put(it, character.getModifiedAbilityScore(it))
            }
            return result
        }

        fun fromScreen(name: String, character: EditableCharacter, characterLevel: NumericRange,
                       editableAbilityMap: EditableAbilityMap): EditableFields
        {
            val result = fromCharacter(character)
            result.level = characterLevel.current
            editableAbilityMap.map.forEach { result.stats[it.key] = it.value.current }
            result.name = name

            // use json serialization to get a deep copy
            result.plan = Json.decodeFromString (Json.encodeToString (character.editableFields.plan))
            return result
        }
    }
}
