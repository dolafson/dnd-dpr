package com.vikinghelmet.dnd.dpr.spells

import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import com.vikinghelmet.dnd.dpr.scenario.EffectWithDuration
import com.vikinghelmet.dnd.dpr.spells.payload.Attack
import com.vikinghelmet.dnd.dpr.spells.payload.Damage
import com.vikinghelmet.dnd.dpr.turn.AttackAction
import com.vikinghelmet.dnd.dpr.turn.AttackResult
import com.vikinghelmet.dnd.dpr.turn.Preconditions
import com.vikinghelmet.dnd.dpr.util.Condition
import com.vikinghelmet.dnd.dpr.util.DiceBlockHelper
import com.vikinghelmet.dnd.dpr.util.TargetEffect
import kotlinx.serialization.Serializable

// https://github.com/nick-aschenbach/dnd-data/blob/main/data/spells.json

object SpellHelper {
    fun getSpellNames(list: List<Spell>): MutableList<String> {
        val result = mutableListOf<String>()
        for (spell in list) result.add(spell.name)
        return result
    }
}

@Serializable
open class Spell(
    val book: String,
    val description: String,
    val name: String,
    val properties: Properties,
    val publisher: String
) : AttackAction, EffectWithDuration {
    // override fun getActionName(): String { return name }

    override fun toString(): String {
        return name
    }

    fun is2014(): Boolean {
        return book.endsWith("(2014)")
    }

    fun isBonusAction(): Boolean {
        return properties.CastingTime == "Bonus Action"
    }

    fun triggersSavingThrow(): Boolean {
        for (a in getSpellAttacks()) if (a.isSavingThrowAttack()) return true
        return false
    }

    fun takeImmediatelyAfterHitting(): Boolean {
        val dd = properties.dataDescription ?: ""
        return dd.startsWith("Bonus Action, which you take immediately after hitting")
    }

    fun isMeleeWeaponBonusAction(): Boolean { // only a few of these exist
        val dd = properties.dataDescription ?: ""
        return takeImmediatelyAfterHitting() && (dd.contains("Melee weapon") || dd.contains("with a weapon"))
    }

    fun isRangedWeaponBonusAction(): Boolean { // only a few of these exist
        val dd = properties.dataDescription ?: ""
        return takeImmediatelyAfterHitting() && (dd.contains("Ranged weapon") || dd.contains("with a weapon"))
    }

    /* notes on RANGE data

        2024 spells:
            {"Range":"10 feet","filter-Range":"Close (30 feet or less)","data-RangeNum":10}
            {"Range":"120 feet","filter-Range":"Far (more than 60 feet)","data-RangeNum":120}
            {"Range":"Self","filter-Range":"Self","data-RangeNum":null}

        2014 spells:
            {"Range":"10 feet","filter-Range":null,"data-RangeNum":10}
            {"Range":"120 feet","filter-Range":null,"data-RangeNum":120}
            {"Range":"Self (10-foot radius)","filter-Range":null,"data-RangeNum":-6}
     */

    fun getRange(): Int {
        return properties.dataRangeNum ?: 0
    }

    fun isSameIn2014And2024(): Boolean {
        return book.endsWith("(2014 and 2024)")
    }

    // TODO: find a way to model spells with delayed effect, such as 2014 Hail of Thorns:
    // concentration up to 1 min, but only 1 instant of damage
    // note, in 2024 the spell was changed to Instantaneous (Bonus Action)
    override fun getDuration(): Int? {
        val dur = properties.filterDuration ?: return null

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

                val maxNumberOfTargets = if (d.payload.aoe != null) 3 else 1 // TODO: improve this

                attackList.add(SpellAttack(d.payload, damagePayload, maxNumberOfTargets))
            }
        }

        if (attackList.isEmpty()) {
            attackList.add(SpellAttack(Attack(name = this.name), null, 1))
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
        for (spellAttack in getSpellAttacks()) {
            val abilityName = spellAttack.getSaveAbility()
            if (abilityName.isNotEmpty()) {
                return AbilityType.valueOf(abilityName)
            }
        }
        return null
    }

    private fun appliesToNextAttackOnly(): Boolean {
        return (getDuration() ?: 0) <= 1 // TODO: is this the best representation ?
    }

    override fun appliesToNextMeleeOrRangeAttackOnly(): Boolean {
        if (!appliesToNextAttackOnly()) return false
        for (spellAttack in getSpellAttacks()) {
            if (spellAttack.isMeleeOrRangeAttack()) return true
        }
        return false
    }

    override fun appliesEffectToNextTargetSaveOnly(): Boolean {
        val effect = getTargetEffect()
        return appliesToNextAttackOnly() &&
                (effect.disadvantageOnSave.isNotEmpty() || effect.savePenalty.isNotEmpty() || effect.autoFailSave.isNotEmpty())
    }

    override fun getTargetEffect(): TargetEffect {
        val result = TargetEffect()
        val conditions = getSpellFailConditions()
        for (cond in conditions) {
            result.applyCondition(cond)
        }

        result.applySpellName(name)
        return result
    }

    fun postProcessEffectsOfOldSpells(oldSpells: List<Spell>, attackResult: AttackResult) {
        val saveAbility = getSpellSaveAbility()
        for (oldSpell in oldSpells) {
            val effect = oldSpell.getTargetEffect()
            for (ability in effect.disadvantageOnSave) {
                if (ability == AbilityType.ALL  || ability == saveAbility) {
                    attackResult.targetHadDisadvantageOnSave = true
                }
            }
        }
    }

    fun preProcessEffectsOfOldSpell(oldSpell: EffectWithDuration, precondition: Preconditions) {
        val effect = oldSpell.getTargetEffect()
        val saveAbility = getSpellSaveAbility()

        for (ability in effect.autoFailSave) {
            if (ability == AbilityType.ALL || ability == saveAbility) {
                precondition.autoFailSave = true
            }
        }

        for (penalty in effect.savePenalty) {
            if (effect.savePenaltyFilter.isEmpty() ||
                effect.savePenaltyFilter.contains(AbilityType.ALL) ||
                effect.savePenaltyFilter.contains(saveAbility))
            {
                if (precondition.penaltyDiceToSave == null) {
                    precondition.penaltyDiceToSave = DiceBlockHelper.get(penalty)
                }
                else {
                    precondition.penaltyDiceToSave!! += DiceBlockHelper.get(penalty)
                }
            }
        }
    }

}