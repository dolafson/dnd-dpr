package com.vikinghelmet.dnd.dpr.character.feats

import kotlinx.serialization.Serializable

@Serializable
data class FeatAdded(
    val componentId: Int,
    val componentTypeId: Int,
    val definition: Definition,
    val definitionId: Int
)