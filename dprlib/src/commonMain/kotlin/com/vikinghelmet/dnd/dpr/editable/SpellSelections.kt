@file:OptIn(ExperimentalSerializationApi::class)

package com.vikinghelmet.dnd.dpr.editable

import com.vikinghelmet.dnd.dpr.spells.Spell
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class SpellSelections (
    //var level: Int,
    var readOnlyList: MutableList<Spell> = mutableListOf(),
    var editableList: MutableList<Spell> = mutableListOf(), // spell selections that are editable at current character level
){
    fun isEmpty(): Boolean { return readOnlyList.isEmpty() && editableList.isEmpty() }
    fun isNotEmpty(): Boolean { return readOnlyList.isNotEmpty() || editableList.isNotEmpty() }

    fun size(): Int = readOnlyList.size + editableList.size

}
