package com.vikinghelmet.dnd.dpr.action

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
}