package com.vikinghelmet.dnd.dpr.turn

import com.vikinghelmet.dnd.dpr.character.Character
import com.vikinghelmet.dnd.dpr.character.inventory.Weapon
import com.vikinghelmet.dnd.dpr.spells.SpellAttack
import com.vikinghelmet.dnd.dpr.util.DiceBlock
import com.vikinghelmet.dnd.dpr.util.DiceBlockHelper
import kotlinx.serialization.Serializable

@Serializable
data class MeleeOrRangeAttack(
    // required fields
    val character: Character, // need this to compute weapon damage, attack bonus, etc
    val spellAttack: SpellAttack?, // name of spell or weapon
    val weapon: Weapon?,
) {
    fun getBonusDamage(isBonusAction: Boolean): Int {
        return if (weapon != null) character.getDamageBonus(weapon, isBonusAction) else 0
    }

    fun getBonusToHit(isBonusAction: Boolean): Int {
        // TODO: support separate attack bonus for BonusAttack
        return if (weapon != null) character.getAttackBonus(weapon) else character.getSpellBonusToHit()
    }

    fun getDamageDice(): DiceBlock {
        if (weapon != null) {
            return weapon.getDamageDice()
        }
        // note: spells that scale directly based on character level (total level, not just caster class level)
        // are exclusively cantrips, which increase in damage at 5th, 11th, and 17th levels.
        if (spellAttack != null) {
            return spellAttack.getDamageDice()
        }
        println("invalid attack: weapon/spell not found")
        return DiceBlockHelper.emptyBlock()
    }

    override fun toString(): String {
        return if (spellAttack != null) spellAttack.toString() else if (weapon != null) return weapon.name else "unknown"
    }
}