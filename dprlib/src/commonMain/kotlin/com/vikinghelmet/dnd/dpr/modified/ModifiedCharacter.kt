package com.vikinghelmet.dnd.dpr.modified

import com.vikinghelmet.dnd.dpr.character.Character
import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class ModifiedCharacter (
    val from: Character,
    val characterOverrides: CharacterOverrides
) : Character(from.characterData, from.id, from.message, from.success)
{
    override fun getModifiedAbilityScore(a: AbilityType): Int {
        return characterOverrides.stats.getByAbilityType(a)
    }

    // not sure if override is needed when it is an exact match?
    override fun getStatBlock(): StatBlock {
        return StatBlock(
            getModifiedAbilityScore(AbilityType.Strength),
            getModifiedAbilityScore(AbilityType.Dexterity),
            getModifiedAbilityScore(AbilityType.Constitution),
            getModifiedAbilityScore(AbilityType.Intelligence),
            getModifiedAbilityScore(AbilityType.Wisdom),
            getModifiedAbilityScore(AbilityType.Charisma)
        )
    }
    override fun getLevel(): Int {
        return characterOverrides.level
    }

    override fun getName(): String {
        return characterOverrides.name
    }

}