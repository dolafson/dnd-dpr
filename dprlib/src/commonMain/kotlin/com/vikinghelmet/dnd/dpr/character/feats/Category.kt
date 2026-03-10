package com.vikinghelmet.dnd.dpr.character.feats

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val definitionKey: String,
    val entityId: Int,
    val entityTagId: Int,
    val entityTypeId: Int,
    val id: Int,
    val tagName: String
)