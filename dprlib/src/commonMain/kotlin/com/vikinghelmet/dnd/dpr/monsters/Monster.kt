package com.vikinghelmet.dnd.dpr.monsters

import com.vikinghelmet.dnd.dpr.action.Combatant
import com.vikinghelmet.dnd.dpr.character.actions.ActionAdded
import com.vikinghelmet.dnd.dpr.character.actions.ActionModifier
import com.vikinghelmet.dnd.dpr.character.feats.Feat
import com.vikinghelmet.dnd.dpr.character.inventory.Weapon
import com.vikinghelmet.dnd.dpr.character.inventory.WeaponProperty
import com.vikinghelmet.dnd.dpr.character.race.RacialTrait
import com.vikinghelmet.dnd.dpr.character.spells.PreparedSpell
import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import com.vikinghelmet.dnd.dpr.character.stats.AbilityType.*
import com.vikinghelmet.dnd.dpr.scenario.ActionsAvailable
import com.vikinghelmet.dnd.dpr.util.Constants
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// https://github.com/nick-aschenbach/dnd-data/blob/main/data/monsters.json

@Serializable
data class Monster(
    val book: String,
    val description: String,
    @SerialName("name") val monsterName: String,
    val properties: MonsterProperties,
    val publisher: String
) : Combatant {
    override fun isEvasive() = properties.isEvasive()

    override fun is2014() = false // don't think this matters for monster
    override fun getAC() = properties.dataAcNum
    override fun getName() = monsterName
    override fun getLevel() = 0

    override fun isFeatEnabled(requested: Feat) = false
    override fun isRacialTraitEnabled(requested: RacialTrait) = false
    override fun getAbilityModifier(abilityType: AbilityType) = properties.getAbilityModifier(abilityType)

    override fun getWeaponList(): List<Weapon> {
        if (properties.dataActions == null) return emptyList()
        return properties.dataActions.map { Weapon(it) }.toList()
    }

    // TODO: monster spell casing support
    override fun getPreparedBonusActionSpells(targetProximity: Int): List<PreparedSpell> = emptyList()
    override fun getSpellAbilityBonusWithoutPB(): Int = 0
    override fun getSpellSlots(): List<Int> = MutableList(20) { 0 }
    override fun getSpellBonusToHit(): Int = 0
    override fun getSpellSaveDC(): Int = 0

    override fun getActionModifiersAvailable(): List<ActionModifier> = emptyList()
    override fun getActionList(): List<ActionAdded> = emptyList()
    override fun getExtraAttacks() = 0

    override fun getActionsAvailable(): ActionsAvailable { // TODO: duplication between this and PC
        val actionsAvailable = ActionsAvailable()
        val weaponListNames = mutableListOf<String>()

        for (weapon in getWeaponList()) {
            weaponListNames.add(weapon.name)
            actionsAvailable.add(weapon.range ?: 0, weapon)

            if (weapon.hasWeaponProperty(WeaponProperty.Thrown)) {
                actionsAvailable.add(
                    Constants.MELEE_RANGE,
                    weapon
                ) // this ensures it will appear in both melee and range selection
            }
        }
        // TODO: spells
        return actionsAvailable
    }
}

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
    val dataActions: List<MonsterAction>? = null,
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
    fun getAbilityModifier(abilityType: AbilityType): Int {
        return when (abilityType) {
            Strength -> dataSTRmod
            Dexterity -> dataDEXmod
            Constitution -> dataCONmod
            Intelligence -> dataINTmod
            Wisdom -> dataWISmod
            Charisma -> dataCHAmod
            else -> throw IllegalArgumentException("invalid mod name: {$abilityType}")
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