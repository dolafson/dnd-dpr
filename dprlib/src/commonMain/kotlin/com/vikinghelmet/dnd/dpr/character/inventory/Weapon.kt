package com.vikinghelmet.dnd.dpr.character.inventory

import com.vikinghelmet.dnd.dpr.character.Character
import com.vikinghelmet.dnd.dpr.turn.AttackAction
import com.vikinghelmet.dnd.dpr.turn.MeleeOrRangeAction
import com.vikinghelmet.dnd.dpr.util.DiceBlock
import com.vikinghelmet.dnd.dpr.util.DiceBlockHelper
import kotlinx.serialization.Serializable

@Serializable
open class Weapon (
    val name: String,
    val nickname: String? = null,
    val item: InventoryItem
) : MeleeOrRangeAction, AttackAction
{
    val magic      = item.definition.magic
    val attackType = item.definition.attackType ?: 1    // 1=melee, 2=range
    val range      = item.definition.range ?: 5
    val longRange  = item.definition.longRange
    val damage     = item.definition.damage?.diceString ?: "0d4"

    override fun getActionName(): String { return name }

    override fun getBonusDamage(character: Character, isBonusAction: Boolean): Int {
        return getMagicBonus() + character.getDamageBonus(this, isBonusAction)
    }

    override fun getBonusToHit(character: Character, isBonusAction: Boolean): Int {
        return getMagicBonus() + character.getAttackBonus(this)
    }

    override fun getDamageDice(): DiceBlock {
        return DiceBlockHelper.get(damage)
    }

    fun getPropertyNames() = item.definition.properties?.map {it.name}

    // other methods
    fun hasWeaponProperty(prop: WeaponProperty): Boolean {
        return getPropertyNames()?.contains(prop.name) == true
    }

    fun hasMasteryProperty(prop: MasteryProperty): Boolean {
        return getPropertyNames()?.contains(prop.name) == true
    }

    // for now, just handle magic weapons that get the same bonus to attack and damage
    fun getMagicBonus(): Int {
        return item.definition.grantedModifiers?.firstOrNull {
            it.type == "bonus" && it.subType == "magic" && it.modifierTypeId == 1 && it.modifierSubTypeId == 312
        }?.value ?: 0
    }

    override fun toString(): String {
        return name
    }

     fun toFullString(): String {
        return "Weapon(name='$name', damage=$damage, properties=${getPropertyNames()}, magic=$magic, attackType=$attackType, range=$range, longRange=$longRange, nickname=$nickname, magicBonus=${getMagicBonus()})"
    }

    // override fun getActionName(): String { return name }
}
