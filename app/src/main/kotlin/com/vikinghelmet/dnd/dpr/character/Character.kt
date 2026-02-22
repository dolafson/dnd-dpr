package com.vikinghelmet.dnd.dpr.character

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

val levelToProficiencyMap: Map<Int, Int> = mapOf(
    1 to 2,
    2 to 2,
    3 to 2,
    4 to 2,
    5 to 3,
    6 to 3,
    7 to 3,
    8 to 3,
    9 to 4,
    10 to 4,
    11 to 4,
    12 to 4,
    13 to 5,
    14 to 5,
    15 to 5,
    16 to 5,
    17 to 6,
    18 to 6,
    19 to 6,
    20 to 6,
)

val statToBonusMap: Map<Int, Int> = mapOf(
    1 to -5,
    2 to -4,
    3 to -4,
    4 to -3,
    5 to -3,
    6 to -2,
    7 to -2,
    8 to -1,
    9 to -1,
    10 to 0,
    11 to 0,
    12 to 1,
    13 to 1,
    14 to 2,
    15 to 2,
    16 to 3,
    17 to 3,
    18 to 4,
    19 to 4,
    20 to 5,
    21 to 5,
    22 to 6,
    23 to 6,
    24 to 7,
    25 to 7,
    26 to 8,
    27 to 8,
    28 to 9,
    29 to 9,
    30 to 10,
)

@JsonIgnoreUnknownKeys
@Serializable
data class Character(
    @SerialName("data")
    val characterData: Data,
    val id: Int,
    val message: String,
    val success: Boolean
) {
    fun getRawAbilityScore(a: AbilityType): Int {
        for (stat in characterData.stats) {
            if (stat.id == a.ordinal) return stat.value
        }
        return 0 // should not get here
    }

    fun getModifiedAbilityScore(a: AbilityType): Int {
        val raw = getRawAbilityScore(a)
        var mod = 0
        for (modifier in characterData.modifiers.race) {
            if (modifier.type == "bonus" && modifier.entityId == a.ordinal) {
                mod = (modifier.value?: 0)
                break
            }
        }
        return raw + mod
    }

    fun getLevel(): Int {
        return characterData.classes.first().level
    }

    fun getProficiencyBonus(): Int {
        return levelToProficiencyMap[getLevel()] ?: 0
    }

    fun getSpellSaveDC(): Int {
        val abilityId = characterData.classes.first().definition.spellCastingAbilityId
        val statBonus = if (abilityId == null) 0 else {
            statToBonusMap[getModifiedAbilityScore(AbilityType.entries[abilityId])] ?: 0
        }
        return 8 + statBonus + getProficiencyBonus()
    }

    fun isLucky(): Boolean { // halfling luck feature
        for (trait in characterData.race.racialTraits) {
            if (trait.definition.id == 13856136 && trait.definition.name == "Luck") return true
        }
        return false
    }

    fun test() {
        println ("")
        for (ability in AbilityType.entries) {
            if (ability == AbilityType.unused) continue
            val raw = getRawAbilityScore(ability)
            val mod = getModifiedAbilityScore(ability)
            println ("$ability: raw=$raw, mod=$mod")
        }

        val abilityId = characterData.classes.first().definition.spellCastingAbilityId
        val spellAbilityType = if (abilityId == null) "n/a" else AbilityType.entries[abilityId]

        println ("")
        println ("level         = "+getLevel())
        println ("PB            = "+getProficiencyBonus())
        println ("spell ability = "+spellAbilityType)
        println ("spellSaveDC   = "+getSpellSaveDC())
        println ("isLucky       = "+isLucky())
        println ("")
    }
}
