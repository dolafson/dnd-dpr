package com.vikinghelmet.dnd.dpr.character.inventory

import com.vikinghelmet.dnd.dpr.action.AttackAction
import com.vikinghelmet.dnd.dpr.action.Damage
import com.vikinghelmet.dnd.dpr.action.DamageType
import com.vikinghelmet.dnd.dpr.action.MeleeOrRangeAction
import com.vikinghelmet.dnd.dpr.character.PlayerCharacter
import com.vikinghelmet.dnd.dpr.monsters.MonsterAction
import com.vikinghelmet.dnd.dpr.scenario.Scenario
import com.vikinghelmet.dnd.dpr.util.DiceBlock
import kotlinx.serialization.Serializable

@Serializable
open class Weapon (val name: String) : MeleeOrRangeAction, AttackAction
{
    var nickname: String? = null
    var attackType : Int = 1    // 1=melee, 2=range
    var range : Int = 5
    var longRange : Int? = null

    var bonusToHit: Int? = null
    var propertyNames: List<String> = emptyList()
    var _damageList = mutableListOf<Damage>()

    constructor(other: Weapon) : this(other.name) { // to construct a weapon with no abilityBonus
        this.nickname = other.nickname
        this.attackType = other.attackType
        this.range = other.range
        this.longRange = other.longRange
        this.bonusToHit = other.bonusToHit
        this.propertyNames = other.propertyNames
        this._damageList = other._damageList

        val otherDamage = other.getDamageList().firstOrNull()
        if (otherDamage != null) {
            _damageList.add (Damage (otherDamage.dice.copy(), otherDamage.bonus, 0, otherDamage.type))
        }
    }

    constructor(name: String, nickname: String?, item: PCInventoryItem, pc: PlayerCharacter) : this(name)
    {
        this.nickname = nickname
        // magic  = item.definition.magic
        attackType = item.definition.attackType ?: 1    // 1=melee, 2=range
        range      = item.definition.range ?: 5
        longRange  = item.definition.longRange
        propertyNames = item.definition.properties?.map {it.name} ?: emptyList()

        var magicBonus = item.definition.grantedModifiers?.firstOrNull {
            it.type == "bonus" && it.subType == "magic" && it.modifierTypeId == 1 && it.modifierSubTypeId == 312
        }?.value ?: 0

        bonusToHit = magicBonus + pc.getAttackBonus(this)

        val proficiencyBonus = pc.getDamageBonus(this, false)

        // PC weapons almost always do a single form of damage
        _damageList.add (
            Damage(DiceBlock(item.definition.damage?.diceString ?: "0d4"),
            magicBonus, proficiencyBonus,
            DamageType.valueOf(item.definition.damageType!!.lowercase())))
    }

    constructor(action: MonsterAction) : this(action.Name)
    {
        if (action.Type == "Melee") {
            attackType = 1
            range = 5
            longRange = null
        }
        else { // Ranged
            attackType = 2
            val reach      = action.Reach?.replace(" .*".toRegex(), "") ?: "0/0"  //  "30/120 ft.",
            val reachSplit = reach.split("/")
            range     = reachSplit[0].toInt()
            longRange = reachSplit[1].toInt()
        }

        if (action.HitBonus != null) {
            bonusToHit = action.HitBonus.toInt()
        }

        // 2014 monster data has some ill-defined attacks, such as dragon's "Multiattack" and Breath weapon (damage currently null)
        if (action.Damage != null) {
            _damageList.add(getDamage(action.Damage, action.DamageType!!))
        }

        // monster attacks sometimes do two types of damage (eg. piercing & poison), but never 3
        if (action.Damage2 != null) {
            _damageList.add(getDamage(action.Damage2, action.Damage2Type!!))
        }
    }

    private fun getDamage(diceStringWithBonus: String, damageTypeString: String): Damage {
        val damageSplit = diceStringWithBonus.split("+")
        val beforePlus = damageSplit[0].trim()
        val afterPlus = if (damageSplit.size == 1) 0 else damageSplit[1].trim().toInt()
        return Damage(DiceBlock(beforePlus), afterPlus, 0, DamageType.valueOf(damageTypeString.lowercase()))
    }

    override fun getActionName(): String { return name }
    override fun getDamageList()  = _damageList
    override fun getAttackBonus() = bonusToHit ?: 0
    override fun getNumTargetsAffected(scenario: Scenario) = 1

    // other methods
    fun hasWeaponProperty(prop: WeaponProperty): Boolean {
        return propertyNames.contains(prop.name) == true
    }

    fun hasMasteryProperty(prop: MasteryProperty): Boolean {
        return propertyNames.contains(prop.name) == true
    }

    override fun toString(): String {
        return name
    }

}
