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
import com.vikinghelmet.dnd.dpr.monsters.actions.LegendaryAction
import com.vikinghelmet.dnd.dpr.monsters.actions.MonsterAction
import com.vikinghelmet.dnd.dpr.monsters.actions.Reaction
import com.vikinghelmet.dnd.dpr.monsters.armor.ArmorClass
import com.vikinghelmet.dnd.dpr.scenario.ActionsAvailable
import com.vikinghelmet.dnd.dpr.spells.SpellLikeAction
import com.vikinghelmet.dnd.dpr.util.Constants
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Monster(
    val actions: List<MonsterAction> ?= emptyList(),
    val alignment: String,
    val armor_class: List<ArmorClass>,
    val challenge_rating: Float,
    val charisma: Int,
    val condition_immunities: List<ConditionImmunity>,
    val constitution: Int,
    val damage_immunities: List<String>,
    val damage_resistances: List<String>,
    val damage_vulnerabilities: List<String>,
    val dexterity: Int,
    val desc: String ?= null,
    val forms: List<Form>?= emptyList(),

    val hit_dice: String,
    val hit_points: Int,
    val hit_points_roll: String,
    val image: String,
    val index: String,
    val intelligence: Int,
    val languages: String,
    val legendary_actions: List<LegendaryAction> ?= emptyList(),
    @SerialName("name") val monsterName: String,
    val proficiencies: List<Proficiency>,
    val proficiency_bonus: Int,
    val reactions: List<Reaction> ?= emptyList(),
    val senses: Senses,
    val size: String,
    val special_abilities: List<SpecialAbility> ?= emptyList(),
    val speed: Speed,
    val strength: Int,
    val subtype: String ?= null,
    val type: String,
    val url: String,
    val wisdom: Int,
    val xp: Int
) : Combatant {
    override fun is2014() = true

    override fun getAC(): Int {
        return armor_class.sumOf { it.value }
    }

    override fun getName() = monsterName

    override fun getLevel() = 0

    override fun isFeatEnabled(requested: Feat) = false

    override fun isRacialTraitEnabled(requested: RacialTrait) = false // TODO

    override fun isEvasive() = special_abilities?.any { it.name == "Evasion" } ?: false

    override fun getAbilityModifier(abilityType: AbilityType): Int {
        val score = when (abilityType) {
            Strength -> strength
            Dexterity -> dexterity
            Constitution -> constitution
            Intelligence -> intelligence
            Wisdom -> wisdom
            Charisma -> charisma
            else -> throw IllegalArgumentException("invalid mod name: {$abilityType}")
        }
        return Constants.statToBonusMap[score] ?: 0
    }

    override fun getWeaponList(): List<Weapon>
    {
        if (actions == null) return emptyList()
        return actions.filter { it.dc == null }.map { Weapon(it) }.toList()
    }

    override fun getSpellBonusToHit() = 0 // TODO

    override fun getSpellSaveDC(): Int {
        val dc = actions?.firstNotNullOf { it.dc } // TODO: monsters with multiple dc's ?
        if (dc != null) {
            return dc.dc_value
        }
        return 0
    }

    override fun getPreparedBonusActionSpells(targetProximity: Int) = emptyList<PreparedSpell>() // TODO

    override fun getSpellAbilityBonusWithoutPB()= 0 // TODO

    override fun getSpellSlots() = emptyList<Int>() // TODO

    override fun getActionsAvailable(): ActionsAvailable { // TODO: duplication between this and PC
        val actionsAvailable = ActionsAvailable()
        if (actions == null) return ActionsAvailable()

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

        actions.filter { it.dc != null }.forEach { action ->
            val spell = SpellLikeAction (action)

            if (spell.isRangedSpellAttack()) {
                actionsAvailable.add(spell.getRange(), spell)
            } else {
                // all spells - except for "ranged spell attack" - can be used in melee
                actionsAvailable.add(Constants.MELEE_RANGE, spell)
            }
        }

        return actionsAvailable
    }

    override fun getActionModifiersAvailable() = emptyList<ActionModifier>() // TODO

    override fun getActionList() = emptyList<ActionAdded>() // TODO

    override fun getExtraAttacks()= 0 // TODO

    fun expandMultiAttack(): List<Weapon>
    {
        if (actions == null) return emptyList()

        val result       = mutableListOf<Weapon>()
        val nameToWeapon = getWeaponList().associateBy { it.name }
        val multiAttack  = actions.firstOrNull { it.name == "Multiattack" }

        multiAttack!!.actions!!.forEach { it2 ->
            val weapon = nameToWeapon[it2.action_name]
            repeat(it2.count) { result.add(weapon!!) }
        }

        return result
    }

}