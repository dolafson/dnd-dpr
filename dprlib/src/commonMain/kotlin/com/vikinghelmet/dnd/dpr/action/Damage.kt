package com.vikinghelmet.dnd.dpr.action

import com.vikinghelmet.dnd.dpr.action.enums.DamageType
import com.vikinghelmet.dnd.dpr.util.DiceBlock
import dev.shivathapaa.logger.api.LoggerFactory
import kotlinx.serialization.Serializable

@Serializable
class Damage(val dice: DiceBlock, var bonus: Int, var abilityBonus: Int, val type: DamageType) {

    @kotlinx.serialization.Transient
    private val logger = LoggerFactory.get(Damage::class.simpleName ?: "")

    constructor(from: Damage, isBonus: Boolean?): this(
        from.dice,
        if (isBonus == true) 0 else from.bonus,
        if (isBonus == true) 0 else from.abilityBonus,
        from.type)

/*
    init{
        if (type == DamageType.undefined) {
            try {
                throw Exception("Damage undefined")
            } catch (e: Exception) {
                val stackTraceString: String = e.stackTraceToString()
                logger.debug { stackTraceString }
            }
        }
    }
*/
    override fun toString(): String {
        val builder = StringBuilder("$dice")
        if (bonus > 0) { builder.append(" + $bonus") }
        if (abilityBonus > 0) { builder.append(" + $abilityBonus") }
        builder.append(" $type")
        return builder.toString()
    }

    fun isEmpty() = dice.isEmpty() && bonus == 0
    fun isNotEmpty() = !isEmpty()

    companion object {
        fun fromStringPair(diceStringWithBonus: String, damageTypeString: String): Damage {
            val damageSplit = diceStringWithBonus.split("+")
            val beforePlus = damageSplit[0].trim()
            val afterPlus = if (damageSplit.size == 1) 0 else damageSplit[1].trim().toInt()
            return Damage(DiceBlock(beforePlus), afterPlus, 0, DamageType.valueOf(damageTypeString.lowercase()))
        }

    }
}