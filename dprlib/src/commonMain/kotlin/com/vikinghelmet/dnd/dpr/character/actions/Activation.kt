package com.vikinghelmet.dnd.dpr.character.actions

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class Activation(
    //val activationTime: Any,
    val activationType: Int
)