@file:OptIn(ExperimentalSerializationApi::class)

package com.vikinghelmet.dnd.dpr.editable

import com.vikinghelmet.dnd.dpr.character.feats.Feat
import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class LevelPlan (
    var level: Int,
    var spells: List<String> = mutableListOf(),
    var asi: Map<AbilityType, Int> = mutableMapOf(),    // ability score improvement
    var feat: Feat? = null,
    var subclass: String? = null,                       // for most classes this occurs at 3rd level
    var props: Map<String,String> = mutableMapOf(),     // place for storing feat-related selections
){
}
