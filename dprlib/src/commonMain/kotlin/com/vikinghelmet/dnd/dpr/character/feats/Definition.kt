@file:OptIn(ExperimentalSerializationApi::class)

package com.vikinghelmet.dnd.dpr.character.feats

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class Definition(
    val name: String,

    // optional fields ...

    // val activation: Activation,
    val categories: List<com.vikinghelmet.dnd.dpr.character.feats.Category>? = null,
    //val creatureRules: List<Any>,
    val definitionKey: String? = null,
    val description: String? = null,
    val entityTypeId: Int? = null,
    val id: Int? = -1,
    val isHomebrew: Boolean? = null,
    val isRepeatable: Boolean? = null,
    // val prerequisites: List<Prerequisite>,
    //val repeatableParentId: Any,
    val snippet: String? = null,
    //val sourceId: Any,
    //val sourcePageNumber: Any,
    // val sources: List<Source>,
    //val spellListIds: List<Any>
)