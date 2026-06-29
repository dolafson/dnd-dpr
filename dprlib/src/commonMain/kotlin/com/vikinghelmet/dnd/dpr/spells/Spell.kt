package com.vikinghelmet.dnd.dpr.spells

import com.vikinghelmet.dnd.dpr.action.Action
import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import com.vikinghelmet.dnd.dpr.scenario.TargetEffectCause
import com.vikinghelmet.dnd.dpr.spells.payload.Attack
import com.vikinghelmet.dnd.dpr.spells.payload.Damage
import com.vikinghelmet.dnd.dpr.spells.payload.Healing
import com.vikinghelmet.dnd.dpr.util.Condition
import com.vikinghelmet.dnd.dpr.util.DiceBlock
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
) : Action, TargetEffectCause {
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

    // ranged spell attacks get disadvantage when cast during melee; only 9 of these in 2014, 12 in 2024
    fun isRangedSpellAttack() = (properties.SpellAttack != null && properties.SpellAttack == "Ranged")

    fun isAOE(): Boolean {
        return impactMultipleCreatures() || getSpellAttacks(0).any { it.getAoeSize() > 0 }
    }

    fun impactMultipleCreatures(): Boolean =
        (description.contains("Choose any creatures") || description.contains("creatures of your choice"))

    fun isRecurring() = (getDuration() ?: 1) > 1

    fun isCantrip() = (properties.Level == 0)
    fun isHealing(): Boolean {
        return (properties.Healing != null) || (properties.filterTags !=null && properties.filterTags!!.contains("Healing"))
    }

    fun incursDamage() = getSpellAttacks(0).any { ! it.isNoDamageAttack() }

    // TODO: find a way to model spells with delayed effect, such as 2014 Hail of Thorns:
    // concentration up to 1 min, but only 1 instant of damage
    // note, in 2024 the spell was changed to Instantaneous (Bonus Action)
    fun getDuration(): Int? {
        if (SpellsWithComplexRules.WindWall == SpellsWithComplexRules.fromName(name)) {
            logger.warn { "WindWall, force duration = 1 "} // spell only does instantaneous damage; does nothing after round 1
            throw IllegalArgumentException("WindWall, force duration = 1 ")
            return 1
        }
        return getNumberOfTurns(properties.filterDuration)
    }

    fun getCastingTime() = getNumberOfTurns(properties.CastingTime)

    fun getNumberOfTurns(timeField: String?): Int?
    {
        when (timeField) {
            "Instantaneous" -> return 0
            "Permanent" -> return Int.MAX_VALUE
            null -> return null
        }

        val fieldList = timeField.split(" ")
        val firstFieldInt = fieldList[0].toIntOrNull()

        return if (firstFieldInt == null) convertTimeUnit(timeField)
            else firstFieldInt * convertTimeUnit (fieldList[1].lowercase())
    }

    fun convertTimeUnit(timeUnit: String): Int {
        return when (timeUnit.lowercase()) {
            "action", "turn", "round", "rounds" -> 1
            "min", "minute", "minutes" -> 10
            "hour", "hours" -> 600
            "day", "days" -> 600 * 24
            else -> 0
        }
    }

    fun getHealing(): Healing {
        if (properties.Healing != null) { // 2014 spells store healing in this field; 2024 do not use it
            return Healing (DiceBlock (properties.Healing!!))
        }

        val data = properties.dataDatarecords
        if (data != null) {
            val payload = data.filter { it.payload is Healing }.firstOrNull()?.payload
            if (payload != null) {
                return payload as Healing
            }
        }
        return Healing(DiceBlock())
    }

    fun getSpellAttacks(attackBonus: Int): List<SpellAttack> {
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
                    damagePayload = Damage(DiceBlock(properties.Damage ?: "0d4"))
                }

                attackList.add(SpellAttack(d.payload, damagePayload, attackBonus))
            }
        }

        if (attackList.isEmpty()) {
            attackList.add(SpellAttack(Attack(name = this.name), null, attackBonus))
        }

        // println("attackList = $attackList")
        return attackList
    }

    fun getSpellFailConditions(): List<Condition> {
        val result = mutableListOf<Condition>()
        for (a in getSpellAttacks(0)) {
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
        return getSpellAttacks(0).firstNotNullOfOrNull { it.getSaveAbility() }
    }

    override fun equals(other: Any?): Boolean { // TODO: clean this up
        if (this === other) return true
        if (other !is Spell) return false
        if (name != other.name) return false
        return true
    }

}