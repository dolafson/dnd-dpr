package com.vikinghelmet.dnd.dpr.character.feats

import kotlinx.serialization.Serializable

@Serializable
data class FeatAdded(
    val componentId: Int? = null,
    val componentTypeId: Int? = null,
    val definition: Definition,
    val definitionId: Int? = null
)