@file:OptIn(ExperimentalSerializationApi::class)

package com.vikinghelmet.dnd.dpr.character.classes

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class ClassFeature(
    /*
    {"id":10292322,"name":"Foe Slayer","prerequisite":null,
    "description":"<p>The damage die of your Hunter’s Mark is a d10 rather than a d6.</p>",
    "requiredLevel":20,"displayOrder":21,"summary":null,"featuresSectionType":null,"moreDetailsUrl":"/classes/2190882-ranger#FoeSlayer-10292322"}
     */
    val id: Int,
    val name: String,
    // val prerequisites: String?=null,
    val description: String,
    val requiredLevel: Int,
    //val displayOrder: Int,
    //val summary: String,
    //val featuresSectionType: String,
    //val moreDetailsUrl: String,
)