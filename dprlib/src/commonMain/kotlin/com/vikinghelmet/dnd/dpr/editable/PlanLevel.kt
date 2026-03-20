@file:OptIn(ExperimentalSerializationApi::class)

package com.vikinghelmet.dnd.dpr.editable

import com.vikinghelmet.dnd.dpr.character.feats.Feat
import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@Serializable
class PlanSpell(val level: Int, var name: String){
    override fun toString(): String {
        return "($level: $name)"
    }
}

@JsonIgnoreUnknownKeys
@Serializable
data class PlanLevel (
    var feat: Feat? = null,
    var asi1: AbilityType? = null,    // ability score improvement
    var asi2: AbilityType? = null,    // ability score improvement
    var subclass: String? = null,                       // for most classes this occurs at 3rd level
    var props: Map<String,String> = mutableMapOf(),     // place for storing feat-related selections
    var spells: List<String> = mutableListOf(),
){
}
