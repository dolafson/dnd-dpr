package com.vikinghelmet.dnd.dpr.character.inventory

import com.vikinghelmet.dnd.dpr.character.Character
import com.vikinghelmet.dnd.dpr.turn.AttackAction
import com.vikinghelmet.dnd.dpr.turn.MeleeOrRangeAction
import com.vikinghelmet.dnd.dprlib.util.DiceBlock
import com.vikinghelmet.dnd.dprlib.util.DiceBlockHelper
import kotlinx.serialization.Serializable

@Serializable
data class Weapon (
    val name: String,
    val damage: String? = null,

    // data from dndbeyond character sheet
    val properties: List<String>? = null,
    val magic: Boolean? = false,
    val attackType: Int? = 1,    // 1=melee, 2=range
    val range: Int? = 5,
    val longRange: Int? = 5,
    val nickname: String? = null,

) : MeleeOrRangeAction, AttackAction {
    override fun getBonusDamage(character: Character, isBonusAction: Boolean): Int {
        return character.getDamageBonus(this, isBonusAction)
    }

    override fun getBonusToHit(character: Character, isBonusAction: Boolean): Int {
        return character.getAttackBonus(this)
    }

    override fun getDamageDice(): DiceBlock {
        return DiceBlockHelper.get(damage)
    }

    // other methods
    fun isLight(): Boolean { // this may get used often,
        return properties?.contains("Light") == true
    }

    override fun toString(): String {
        return name
    }
    // override fun getActionName(): String { return name }
}
