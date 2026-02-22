@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.vikinghelmet.dnd.dpr.character.feats

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class Definition(
    // val activation: Activation,
    val categories: List<Category>,
    //val creatureRules: List<Any>,
    val definitionKey: String,
    val description: String,
    val entityTypeId: Int,
    val id: Int,
    val isHomebrew: Boolean,
    val isRepeatable: Boolean,
    val name: String,
    // val prerequisites: List<Prerequisite>,
    //val repeatableParentId: Any,
    val snippet: String,
    //val sourceId: Any,
    //val sourcePageNumber: Any,
    // val sources: List<Source>,
    //val spellListIds: List<Any>
)