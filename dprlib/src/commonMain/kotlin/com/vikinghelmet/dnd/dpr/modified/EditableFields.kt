@file:OptIn(ExperimentalSerializationApi::class)

package com.vikinghelmet.dnd.dpr.modified

import com.vikinghelmet.dnd.dpr.character.Character
import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import com.vikinghelmet.dnd.dpr.util.NumericRangeMap
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class EditableFields (
    var remoteId: Int, // cross-reference to original character from dndbeyond
    var level: Int,
    var name: String,
    var stats: MutableMap<AbilityType, Int> = mutableMapOf(),
){
    companion object {
        fun fromCharacter(character: Character): EditableFields {
            println("EditableFields: fromCharacter: remoteId = ${character.characterData.id!!}")
            val result = EditableFields(character.characterData.id!!,
                character.getLevel(), character.getName())

            AbilityType.entries.forEach {
                result.stats.put(it, character.getModifiedAbilityScore(it))
            }
            return result
        }

        fun fromScreen(name: String, character: Character, numericRangeMap: NumericRangeMap): EditableFields {
            val result = fromCharacter(character)
            numericRangeMap.map.forEach {
                if(it.key == "level") {
                    println("level: rangeMap[${it.key}] = "+it.value.current)
                    result.level = it.value.current
                }
                else try {
                    println("ability: rangeMap[${it.key}] = ${it.value.current}")
                    result.stats[AbilityType.valueOf(it.key)] = it.value.current
                }
                catch (e: IllegalArgumentException) {
                    e.printStackTrace()
                }
            }
            result.name = name
            return result
        }
    }
}
