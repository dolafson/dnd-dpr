package com.vikinghelmet.dnd.dpr.monsters

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// https://github.com/nick-aschenbach/dnd-data/blob/main/data/monsters.json

@Serializable
data class Monster(
    val book: String,
    val description: String,
    val name: String,
    val properties: MonsterProperties,
    val publisher: String
)

@Serializable
data class MonsterProperties(
    val AC: String,
    val HP: String,

    val STR: Int,
    val DEX: Int,
    val CON: Int,
    val INT: Int,
    val WIS: Int,
    val CHA: Int,

    val Alignment: String,
    val Category: String,
    @SerialName("Challenge Rating")
    val ChallengeRating: String,
    @SerialName("Condition Immunities")
    val ConditionImmunities: String? = null,
    val Expansion: Int? = null,

    @SerialName("Hit Dice")
    val HitDice: String? = null,
    val Immunities: String? = null,
    val Languages: String? = null,
    @SerialName("Legendary Roll 0")
    val LegendaryRoll0: String? = null,
    val PB: Int? = null,
    @SerialName("Passive Perception")
    val PassivePerception: Int,
    val Resistances: String? = null,

    @SerialName("Roll 0")
    val Roll0: String? = null,
    @SerialName("Roll 1")
    val Roll1: String? = null,
    @SerialName("Roll 2")
    val Roll2: String? = null,
    @SerialName("Roll 3")
    val Roll3: String? = null,
    @SerialName("Roll 4")
    val Roll4: String? = null,

    @SerialName("Saving Throws")
    val SavingThrows: String? = null,
    val Senses: String? = null,
    val Size: String,
    val Skills: String? = null,
    val Source: String? = null,
    val Speed: String,
    @SerialName("Spell Book")
    val SpellBook: String? = null,
    @SerialName("Spellcasting Ability")
    val SpellcastingAbility: String? = null,
    val Token: String,
    @SerialName("Token Size")
    val TokenSize: Int,
    val Type: String,
    val Vulnerabilities: String? = null,

    @SerialName("data-AcNum")
    val dataAcNum: Int,
    @SerialName("data-Actions")
    //val dataActions: String? = null,
    val dataActions: List<Action>? = null,
    @SerialName("data-Bonus Actions")
    val dataBonusActions: String? = null,
    @SerialName("data-CrNum")
    val dataCrNum: Double,

    @SerialName("data-CHA-mod")
    val dataCHAmod: String,
    @SerialName("data-CON-mod")
    val dataCONmod: String,
    @SerialName("data-DEX-mod")
    val dataDEXmod: String,
    @SerialName("data-INT-mod")
    val dataINTmod: String,
    @SerialName("data-STR-mod")
    val dataSTRmod: String,
    @SerialName("data-WIS-mod")
    val dataWISmod: String,

    @SerialName("data-HpNum")
    val dataHpNum: Int,
    @SerialName("data-LANum")
    val dataLANum: Int? = null,
    @SerialName("data-Legendary Actions")
    //val dataLegendaryActions: String? = null,
    val dataLegendaryActions: List<LegendaryAction>? = null,
    @SerialName("data-List")
    val dataList: String,
    @SerialName("data-Reactions")
    val dataReactions: String? = null,
    @SerialName("data-SizeNum")
    val dataSizeNum: Int? = null,
    @SerialName("data-Spells")
    val dataSpells: String? = null,

    @SerialName("data-Traits")
    //val dataTraits: String? = null,
    val dataTraits: List<Trait>? = null,

    @SerialName("data-XP")
    val dataXP: String
) {
    fun getMod(modName: String): Int {
        return when (modName) {
            "Strength" -> dataSTRmod
            "Dexterity" -> dataDEXmod
            "Constitution" -> dataCONmod
            "Intelligence" -> dataINTmod
            "Wisdom" -> dataWISmod
            "Charisma" -> dataCHAmod
            else -> throw IllegalArgumentException("invalid mod name: {$modName}")
        }.toInt()
    }

    fun isEvasive(): Boolean {
        if (dataTraits == null) return false
        for (trait in dataTraits) {
            if (trait.Name == "Evasion") return true
        }
        return false
    }
}