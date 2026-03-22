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
    constructor(character: Character): this(character.characterData.id!!, character.getLevel(), character.getName())

    constructor(name: String, character: EditableCharacter, characterLevel: NumericRange): this(character)
    {
        this.level = characterLevel.current
        this.name = name
        // use json serialization to get a deep copy
        this.plan = Json.decodeFromString (Json.encodeToString (character.editableFields.plan))
    }

    fun toPrettyPlan(): String {
        val buf = StringBuilder()
        for ((key, value) in plan) { buf.append("$key=$value").append("\n") }
        return "[$buf]"
    }

}
