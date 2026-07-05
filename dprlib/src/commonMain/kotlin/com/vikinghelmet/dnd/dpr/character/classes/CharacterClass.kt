@file:OptIn(ExperimentalSerializationApi::class)

package com.vikinghelmet.dnd.dpr.character.classes

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class CharacterClass(
    val definition:         ClassTopDefinition,
    val subclassDefinition: ClassTopDefinition? = null,

    // note: these two data sets look similar, but are not identical ...
    //      data.classes[].classFeatures[].definition
    //      data.classes[].definition.classFeatures

    val classFeatures: MutableList<ClassFeature2> = mutableListOf(),

    val level: Int,
)