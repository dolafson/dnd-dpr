package com.vikinghelmet.dnd.dpr.character.stats

enum class AbilityType {
    ALL,  // keep this here to force ordinal value of Strength=1, also to support use cases that apply to all abilities
    Strength,
    Dexterity,
    Constitution,
    Intelligence,
    Wisdom,
    Charisma;

    fun toShortName(): String {
        return name.uppercase().substring(0,3)
    }

    companion object {
        fun fromShortName(shortName: String): AbilityType? {
            return entries.firstOrNull { it.name.lowercase().startsWith(shortName.lowercase()) }
        }
    }
}