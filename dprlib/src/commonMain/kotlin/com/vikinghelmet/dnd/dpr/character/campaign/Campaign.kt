package com.vikinghelmet.dnd.dpr.character.campaign

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class Campaign(
    // stuff we need
    val id: Int,
    val name: String,

    // nice to have
    val characters: List<CampaignCharacter> = mutableListOf(),

    // not needed
    val description: String,
    val dmUserId: Int,
    val dmUsername: String,
    val link: String,
    val publicNotes: String
)


@JsonIgnoreUnknownKeys
@Serializable
data class CampaignCharacter(
    // val avatarUrl: String,
    // val campaignId: Any,
    val characterId: Int,
    val characterName: String,
    val characterUrl: String,
    val isAssigned: Boolean,
    val privacyType: Int,
    val userId: Int,
    val username: String
)