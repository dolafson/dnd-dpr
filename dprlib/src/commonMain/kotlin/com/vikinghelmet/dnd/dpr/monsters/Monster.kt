package com.vikinghelmet.dnd.dpr.monsters

import com.vikinghelmet.dnd.dpr.action.Action
import com.vikinghelmet.dnd.dpr.action.Combatant
import com.vikinghelmet.dnd.dpr.action.Weapon
import com.vikinghelmet.dnd.dpr.action.enums.AttackType
import com.vikinghelmet.dnd.dpr.action.enums.DamageType
import com.vikinghelmet.dnd.dpr.character.actions.ActionAdded
import com.vikinghelmet.dnd.dpr.character.actions.ActionModifier
import com.vikinghelmet.dnd.dpr.character.feats.Feat
import com.vikinghelmet.dnd.dpr.character.race.RacialTrait
import com.vikinghelmet.dnd.dpr.character.spells.PreparedSpell
import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import com.vikinghelmet.dnd.dpr.character.stats.AbilityType.*
import com.vikinghelmet.dnd.dpr.monsters.actions.LegendaryAction
import com.vikinghelmet.dnd.dpr.monsters.actions.MonsterAction
import com.vikinghelmet.dnd.dpr.monsters.actions.MonsterPrimaryAction
import com.vikinghelmet.dnd.dpr.monsters.actions.Reaction
import com.vikinghelmet.dnd.dpr.monsters.armor.ArmorClass
import com.vikinghelmet.dnd.dpr.scenario.ActionsAvailable
import com.vikinghelmet.dnd.dpr.util.Constants
import com.vikinghelmet.dnd.dpr.util.Movement
import dev.shivathapaa.logger.api.LoggerFactory
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Monster(
    val actions: List<MonsterPrimaryAction> ?= emptyList(),
    val alignment: String,
   // val armor_class: List<ArmorClass>,
    val armor_class: MutableList<ArmorClass> = mutableListOf(),
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
    val legendary_actions: List<LegendaryAction> = emptyList(),
    @SerialName("name") val monsterName: String,
    val proficiencies: List<Proficiency>,
    val proficiency_bonus: Int,
    val reactions: List<Reaction> = emptyList(),
    val senses: Senses,
    val size: String,
    val special_abilities: List<SpecialAbility> = emptyList(),
    val speed: Speed,
    val strength: Int,
    val subtype: String ?= null,
    val type: String,
    val url: String,
    val wisdom: Int,
    val xp: Int
) : Combatant {
    @Transient
    private val logger = LoggerFactory.get(Monster::class.simpleName ?: "")

    @Transient
    val _actionsAvailable = ActionsAvailable()

    override fun is2014() = true

    override fun getAC(): Int {
        return armor_class.sumOf { it.value }
    }

    override fun getHP() = hit_points

    override fun getInitiativeBonus() = getAbilityModifier(Dexterity)

    override fun getName() = monsterName

    override fun toString(): String {
        return getName()
    }

    override fun getSpeed(movement: Movement): Int {
        val speedString: String? = when (movement) {
            Movement.burrow -> speed.burrow
            Movement.climb -> speed.climb
            Movement.hover -> speed.hover
            Movement.fly -> speed.fly
            Movement.swim -> speed.swim
            Movement.walk -> speed.walk
        } as String?
        return speedString?.replace(" .*".toRegex(),"")?.toInt() ?: 0
    }

    override fun getDamageImmunities()      = damage_immunities.map      { DamageType.valueOf(it) }.toList()
    override fun getDamageResistances()     = damage_resistances.map     { DamageType.valueOf(it) }.toList()
    override fun getDamageVulnerabilities() = damage_vulnerabilities.map { DamageType.valueOf(it) }.toList()

    override fun isFeatEnabled(requested: Feat) = false

    override fun isRacialTraitEnabled(requested: RacialTrait) = false // TODO

    override fun isEvasive() = special_abilities?.any { it.name == "Evasion" } ?: false

    override fun getAbilityScore(abilityType: AbilityType): Int {
        return when (abilityType) {
            Strength -> strength
            Dexterity -> dexterity
            Constitution -> constitution
            Intelligence -> intelligence
            Wisdom -> wisdom
            Charisma -> charisma
            else -> throw IllegalArgumentException("invalid mod name: {$abilityType}")
        }
    }

    override fun getAbilityModifier(abilityType: AbilityType): Int {
        return Constants.statToBonusMap[getAbilityScore(abilityType)] ?: 0
    }

    override fun getWeaponList(): List<Weapon>
    {
        if (actions == null) return emptyList()
        return actions.filter { it.dc == null }.map { it.toWeapon() }.toList()
    }

    override fun getSpellBonusToHit() = 0 // TODO

    override fun getSpellSaveDC(): Int {
        if (actions == null) return 0
        val dc = actions.firstNotNullOfOrNull { it.dc } // TODO: monsters with multiple dc's ?
        if (dc != null) {
            return dc.dc_value
        }
        return 0
    }

    override fun getPreparedBonusActionSpells(targetProximity: Int) = emptyList<PreparedSpell>() // TODO

    override fun getSpellSlots() = emptyList<Int>() // TODO

    override fun getActionsAvailable(): ActionsAvailable { // TODO: duplication between this and PC
        if (_actionsAvailable.isEmpty()) {
            initActionsAvailable()
        }
        return _actionsAvailable
    }

    fun initActionsAvailable() {
        // WARNING: since we are caching actions here, we can't do any turn-by-turn filtering (at this level)
        for (weapon in getWeaponList()) {
            _actionsAvailable.add(weapon.range, weapon)

            if (weapon.attackType == AttackType.MeleeOrRange) {
                _actionsAvailable.add(Constants.MELEE_RANGE, weapon)
            }
        }

        if (actions != null) {
            addSavingThrowActions(actions)
        }
        addSavingThrowActions(special_abilities)
        addSavingThrowActions(legendary_actions)
        addSavingThrowActions(reactions)
    }

    private fun addSavingThrowActions(list: List<MonsterAction>) {
        list.filter { it.dc != null }.forEach { a ->
            val spell = a.toSavingThrowAction()

            if (spell.isRangedSpellAttack()) {
                _actionsAvailable.add(spell.getRange(), spell)
            } else {
                // all spells - except for "ranged spell attack" - can be used in melee
                _actionsAvailable.add(Constants.MELEE_RANGE, spell)

                val range = spell.getRange()
                if (range > Constants.MELEE_RANGE) {
                    _actionsAvailable.add(range, spell)
                }
            }
        }
    }

    override fun getActionModifiersAvailable() = emptyList<ActionModifier>() // TODO

    override fun getActionList() = emptyList<ActionAdded>() // TODO

    fun expandMultiAttack(): List<Action>
    {
        if (actions == null) return emptyList()
        val result       = mutableListOf<Action>()
        val nameToWeapon = getWeaponList().associateBy { it.name }
        val multiAttack  = actions.firstOrNull { it.name == "Multiattack" }

        for (subAttack in multiAttack!!.actions!!) {
            val name = subAttack.action_name
            val action = if (name in nameToWeapon) nameToWeapon[name] else
                _actionsAvailable.getPrimaryAction(Constants.MELEE_RANGE).firstOrNull { it.getActionName() == name }

            if (action == null) {
                logger.error { "multiAttack: subAttack not found: $name" }
                continue
            }
            repeat(subAttack.count) {
                result.add(action)
            }
        }

        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is Monster) return false
        return monsterName.equals(other.monsterName)
    }

}