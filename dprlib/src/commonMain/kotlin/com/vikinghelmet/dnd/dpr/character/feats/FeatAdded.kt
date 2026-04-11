package com.vikinghelmet.dnd.dpr.character.feats

import kotlinx.serialization.Serializable

@Serializable
data class FeatAdded(
    val componentId: Int? = null,
    val componentTypeId: Int? = null,
    val definition: com.vikinghelmet.dnd.dpr.character.feats.Definition,
    val definitionId: Int? = null
) {
    fun getFeat() = Feat.fromNameWithWS(definition.name)

    override fun toString(): String {
       return "(name=${definition.name}, isASI=${isASI()}, isFightingStyle=${isFightingStyle()}"//, categories=${definition.categories}"
    }
    fun isFightingStyle(): Boolean {
        return definition.description?.contains("Fighting Style Feat") ?: false
    }

    fun isASI(): Boolean {
        return definition.snippet?.contains("Ability Score Increase") ?: false
    }
}