package com.vikinghelmet.dnd.dpr.action

import com.vikinghelmet.dnd.dpr.action.enums.DamageType
import com.vikinghelmet.dnd.dpr.util.DiceBlock
import kotlinx.serialization.Serializable

@Serializable
class Damage(val dice: DiceBlock, var bonus: Int, var abilityBonus: Int, val type: DamageType) {

    override fun toString(): String {
        val builder = StringBuilder("$dice")
        if (bonus > 0) { builder.append(" + $bonus") }
        if (abilityBonus > 0) { builder.append(" + $abilityBonus") }
        builder.append(" $type")
        return builder.toString()
    }

    companion object {
        fun fromStringPair(diceStringWithBonus: String, damageTypeString: String): Damage {
            val damageSplit = diceStringWithBonus.split("+")
            val beforePlus = damageSplit[0].trim()
            val afterPlus = if (damageSplit.size == 1) 0 else damageSplit[1].trim().toInt()
            return Damage(DiceBlock(beforePlus), afterPlus, 0, DamageType.valueOf(damageTypeString.lowercase()))
        }

    }
}