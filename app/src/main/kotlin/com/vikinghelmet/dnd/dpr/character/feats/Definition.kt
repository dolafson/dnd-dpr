@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.vikinghelmet.dnd.dpr.character.feats

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class Definition(
    // val activation: Activation,
    val categories: List<Category>? = null,
    //val creatureRules: List<Any>,
    val definitionKey: String? = null,
    val description: String? = null,
    val entityTypeId: Int? = null,
    val id: Int,
    val isHomebrew: Boolean? = null,
    val isRepeatable: Boolean? = null,
    val name: String,
    // val prerequisites: List<Prerequisite>,
    //val repeatableParentId: Any,
    val snippet: String,
    //val sourceId: Any,
    //val sourcePageNumber: Any,
    // val sources: List<Source>,
    //val spellListIds: List<Any>
)