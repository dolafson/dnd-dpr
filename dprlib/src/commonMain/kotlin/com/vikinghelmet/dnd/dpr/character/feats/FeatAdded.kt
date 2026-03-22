package com.vikinghelmet.dnd.dpr.character.feats

import kotlinx.serialization.Serializable

@Serializable
data class FeatAdded(
    val componentId: Int? = null,
    val componentTypeId: Int? = null,
    val definition: com.vikinghelmet.dnd.dpr.character.feats.Definition,
    val definitionId: Int? = null
) {
    fun isFightingStyle(): Boolean {
        return definition.description?.contains("Fighting Style Feat") ?: false
    }

    fun isASI(): Boolean {
        return definition.snippet?.contains("Ability Score Increase") ?: false
    }
}