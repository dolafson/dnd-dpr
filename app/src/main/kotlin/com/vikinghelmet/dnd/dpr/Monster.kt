package com.vikinghelmet.dnd.dpr
import kotlinx.serialization.Serializable

@Serializable
data class Monster(
    val name: String,
    val type: String,
    val alignment: String,
    val size: String,
    val challengeRating: Float,
    val armorClass: Int,
    val hitPoints: Int,
    val strength: Int,
    val dexterity: Int,
    val constitution: Int,
    val intelligence: Int,
    val wisdom: Int,
    val charisma: Int,
)