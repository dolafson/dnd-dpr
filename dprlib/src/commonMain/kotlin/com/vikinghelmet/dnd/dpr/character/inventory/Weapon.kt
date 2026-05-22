package com.vikinghelmet.dnd.dpr.character.inventory

import com.vikinghelmet.dnd.dpr.character.Combatant
import com.vikinghelmet.dnd.dpr.monsters.Action
import com.vikinghelmet.dnd.dpr.scenario.Scenario
import com.vikinghelmet.dnd.dpr.turn.AttackAction
import com.vikinghelmet.dnd.dpr.turn.MeleeOrRangeAction
import com.vikinghelmet.dnd.dpr.util.DiceBlock
import com.vikinghelmet.dnd.dpr.util.DiceBlockHelper
import kotlinx.serialization.Serializable

@Serializable
open class Weapon () : MeleeOrRangeAction, AttackAction
{
    var name: String = ""
    var nickname: String? = null
    var magic : Boolean = false
    var attackType : Int = 1    // 1=melee, 2=range
    var range : Int = 5
    var longRange : Int? = null
    var damage = "0d4"
    var magicBonus: Int = 0    // for now, just handle magic weapons that get the same bonus to attack and damage
    var flatBonusDamage: Int? = null
    var flatBonusToHit: Int? = null
    var propertyNames: List<String> = emptyList()

    constructor(other: Weapon, flatBonusDamage: Int) : this() {
        this.name = other.name
        this.nickname = other.nickname
        this.magic = other.magic
        this.attackType = other.attackType
        this.range = other.range
        this.longRange = other.longRange
        this.damage = other.damage
        this.magicBonus = other.magicBonus
        this.flatBonusDamage = flatBonusDamage
        this.propertyNames = other.propertyNames
    }

    constructor(name: String, nickname: String?, item: InventoryItem) : this() {
        this.name = name
        this.nickname = nickname
        magic      = item.definition.magic
        attackType = item.definition.attackType ?: 1    // 1=melee, 2=range
        range      = item.definition.range ?: 5
        longRange  = item.definition.longRange
        damage     = item.definition.damage?.diceString ?: "0d4"
        propertyNames = item.definition.properties?.map {it.name} ?: emptyList()
        magicBonus = item.definition.grantedModifiers?.firstOrNull {
            it.type == "bonus" && it.subType == "magic" && it.modifierTypeId == 1 && it.modifierSubTypeId == 312
        }?.value ?: 0
    }

    constructor(item: Action) : this() {
        this.name = item.Name
        magic     = false // TODO

        if (item.Type == "Melee") {
            attackType = 1
            range = 5
            longRange = null
        }
        else { // Ranged
            attackType = 2
            val reach      = item.Reach?.replace(" .*".toRegex(), "") ?: "0/0"  //  "30/120 ft.",
            val reachSplit = reach.split("/")
            range     = reachSplit[0].toInt()
            longRange = reachSplit[1].toInt()
        }

        if (item.Damage == null) {
            damage = "0d4"
        } else {
            // "Damage": "1d6 + 2",
            val damageSplit = item.Damage.split("+")
            damage = damageSplit[0].trim()
            if (damageSplit.size > 1) {
                flatBonusDamage = damageSplit[1].trim().toInt()
            }
        }
        
        if (item.HitBonus != null) {
            flatBonusToHit = item.HitBonus.toInt()
        }
    }

    override fun getActionName(): String { return name }

    override fun getBonusDamage(combatant: Combatant, isBonusAction: Boolean): Int {
        if (flatBonusDamage != null) return flatBonusDamage!!
        return magicBonus + combatant.getDamageBonus(this, isBonusAction)
    }

    override fun getBonusToHit(combatant: Combatant, isBonusAction: Boolean): Int {
        if (flatBonusToHit != null) return flatBonusToHit!!
        return magicBonus + combatant.getAttackBonus(this)
    }

    override fun getDamageDice(): DiceBlock {
        return DiceBlockHelper.get(damage)
    }

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

     fun toFullString(): String {
        return "Weapon(name='$name', damage=$damage, properties=$propertyNames, magic=$magic, attackType=$attackType, range=$range, longRange=$longRange, nickname=$nickname, magicBonus=$magicBonus)"
    }

    // override fun getActionName(): String { return name }
}
