package com.vikinghelmet.dnd.dpr.spells

import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import com.vikinghelmet.dnd.dpr.spells.payload.Attack
import com.vikinghelmet.dnd.dpr.spells.payload.Damage
import com.vikinghelmet.dnd.dpr.turn.AttackAction
import com.vikinghelmet.dnd.dpr.util.Condition
import com.vikinghelmet.dnd.dpr.util.DiceBlockHelper
import com.vikinghelmet.dnd.dpr.util.TargetEffectCause
import dev.shivathapaa.logger.api.LoggerFactory
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

// https://github.com/nick-aschenbach/dnd-data/blob/main/data/spells.json

@Serializable
open class Spell(
    val book: String,
    val description: String,
    val name: String,
    val properties: Properties,
    val publisher: String
) : AttackAction, TargetEffectCause {
    override fun getActionName(): String { return name }

    @Transient private val logger = LoggerFactory.get(Spell::class.simpleName ?: "")

    override fun toString() = name
    fun fullString() = "book=$book, publisher=$publisher, name=$name, properties=$properties"

    fun is2014()                = book.endsWith("(2014)")
    fun is2014And2024()         = book.endsWith("(2014 and 2024)")
    fun isRitual()              = (properties.Ritual?.contains("Yes") == true)
    fun isBonusAction()         = (properties.CastingTime == "Bonus Action")
    fun getRange()              = properties.dataRangeNum ?: 0
    private fun dd()            = properties.dataDescription ?: ""
    fun takeImmediatelyAfterHitting() = dd().startsWith("Bonus Action, which you take immediately after hitting")
    fun isMeleeWeaponBonusAction()  = takeImmediatelyAfterHitting() && (dd().contains("Melee weapon") || dd().contains("with a weapon"))
    fun isRangedWeaponBonusAction() = takeImmediatelyAfterHitting() && (dd().contains("Ranged weapon") || dd().contains("with a weapon"))


    // TODO: find a way to model spells with delayed effect, such as 2014 Hail of Thorns:
    // concentration up to 1 min, but only 1 instant of damage
    // note, in 2024 the spell was changed to Instantaneous (Bonus Action)
    fun getDuration(): Int? {
        val dur = properties.filterDuration ?: return null

        if (SpellsWithComplexRules.WindWall == SpellsWithComplexRules.fromNameWithWS(name)) {
            logger.warn { "WindWall, force duration = 1 "} // spell only does instantaneous damage; does nothing after round 1
            return 1
        }

        when (dur) {
            "Instantaneous" -> return 0
            "Permanent" -> return Int.MAX_VALUE
        }

        val durList = dur.split(" ")

        return durList[0].toInt() * when (durList[1].lowercase()) {
            "turn", "round" -> 1
            "min", "minute" -> 10
            "hour", "hours" -> 600
            "days" -> 600 * 24
            else -> 0
        }
    }

    fun getSpellAttacks(): List<SpellAttack> {
        val attackList = mutableListOf<SpellAttack>()
        val data = properties.dataDatarecords ?: return attackList
        for (d in data) {
            if (d.payload is Attack) {
                var damagePayload : Damage? = null
                // attach Damage to Attack where applicable
                for (d2 in data) {
                    if (d2.payload is Damage && d2.parent == d.name) {
                        damagePayload = d2.payload
                        break
                    }
                }

                // 2014 spells: damage is stored differently
                if (damagePayload == null) {
                    logger.debug { "spell name=$name properties.Damage = ${properties.Damage}" }
                    damagePayload = Damage(DiceBlockHelper.get(properties.Damage))
                }

                attackList.add(SpellAttack(d.payload, damagePayload))
            }
        }

        if (attackList.isEmpty()) {
            attackList.add(SpellAttack(Attack(name = this.name), null))
        }

        // println("attackList = $attackList")
        return attackList
    }

    fun getSpellFailConditions(): List<Condition> {
        val result = mutableListOf<Condition>()
        for (a in getSpellAttacks()) {
            if (a.isSavingThrowAttack() && a.attackPayload.save?.onFail != null) {
                val onFail = a.attackPayload.save.onFail
                for (cond in Condition.entries) {
                    if (onFail.contains(cond.name) && !result.contains(cond)) result.add(cond)
                }
            }

            // also check spell description, in case details are missing form onFail
            for (cond in Condition.entries) {
                if (description.contains(cond.name) && !result.contains(cond)) result.add(cond)
            }
        }

        return result
    }

    fun getSpellSaveAbility(): AbilityType? {
        return getSpellAttacks().firstNotNullOfOrNull { it.getSaveAbility() }
    }

}