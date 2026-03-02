package com.vikinghelmet.dnd.dpr.character.stats

enum class AbilityType {
    ALL,  // keep this here to force ordinal value of Strength=1, also to support use cases that apply to all abilities
    Strength,
    Dexterity,
    Constitution,
    Intelligence,
    Wisdom,
    Charisma,
}