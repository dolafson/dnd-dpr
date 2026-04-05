package com.vikinghelmet.dnd.dpr.scenario

import com.vikinghelmet.dnd.dpr.character.actions.ActionModifier
import com.vikinghelmet.dnd.dpr.character.feats.FeatWithDuration
import com.vikinghelmet.dnd.dpr.spells.Spell
import com.vikinghelmet.dnd.dpr.spells.SpellAttack
import com.vikinghelmet.dnd.dpr.turn.Attack
import com.vikinghelmet.dnd.dpr.turn.Preconditions
import com.vikinghelmet.dnd.dpr.util.DiceBlockHelper
import com.vikinghelmet.dnd.dpr.util.Globals
import com.vikinghelmet.dnd.dpr.util.TargetEffect

interface EffectWithDuration {
    fun getDuration(): Int?
    fun getTargetEffect(): TargetEffect
    fun appliesToNextMeleeOrRangeAttackOnly(): Boolean
}

data class RunningEffect (val startTurn: Int, val effect: EffectWithDuration)

data class EffectManager(
    val runningEffectList: ArrayList<RunningEffect>
)
{
    fun attackerHasAdvantage() = runningEffectList.any { it.effect.getTargetEffect().attackerHasAdvantage == true }
    fun isAutoCrit()           = runningEffectList.any { it.effect.getTargetEffect().attackerAutoCrit == true }
    fun getRunningSpells()     = runningEffectList.filter { it.effect is Spell }.map { it.effect as Spell }

    override fun toString(): String {
        val buf = StringBuilder()
        for (running in runningEffectList) buf.append(running.effect.getTargetEffect())
        return buf.toString()
    }

    fun add(turnId: Int, effect: EffectWithDuration) {
        if (effect is FeatWithDuration) {
            for (runningEffect in runningEffectList) {
                if (runningEffect.effect is FeatWithDuration && runningEffect.effect.feat == effect.feat) {
                    Globals.debug("no double dipping on feat->effects")
                    return
                }
            }
        }
        runningEffectList.add(RunningEffect(turnId, effect))
    }

    fun pruneEffectsAtEndOfTurn(turnId: Int) {
        val iterator = runningEffectList.listIterator()
        while (iterator.hasNext()) {
            val running  = iterator.next()
            val deltaT   = turnId - running.startTurn
            val duration = running.effect.getDuration() ?: 0
            if (deltaT >= duration) {
                Globals.debug("effect is complete, remove from running list: "+running.effect.toString())
                iterator.remove()
            }
        }
    }

    fun pruneEffectsWaitingForNextAttack(spellAttack: SpellAttack?) {
        val iterator = runningEffectList.listIterator()
        while (iterator.hasNext()) {
            val running = iterator.next()
            val oneShot = (running.effect.getDuration() ?: 0) <= 1

            if (running.effect.getTargetEffect().hasSaveImpact() && oneShot && spellAttack != null && spellAttack.isSavingThrowAttack()) {
                Globals.debug("effect was waiting for next saving throw, removing it from running list: "+running.effect.toString())
                iterator.remove()
            }

            if (running.effect.appliesToNextMeleeOrRangeAttackOnly() && (spellAttack == null || spellAttack.isMeleeOrRangeAttack())) {
                Globals.debug("effect was waiting for next melee/range attack, removing it from running list: "+running.effect.toString())
                iterator.remove()
            }
        }
    }

    fun getPreconditions(attack: Attack, currentSpell: Spell?): Preconditions {
        val precondition = Preconditions()

        for (action in attack.actionModifiers) {
            when (action) {
                ActionModifier.ColossusSlayer -> precondition.bonusDamageDice += DiceBlockHelper.get("1d8")
                ActionModifier.DreadfulStrike -> precondition.bonusDamageDice += DiceBlockHelper.get("2d6")
                ActionModifier.PolarStrikes   -> precondition.bonusDamageDice += DiceBlockHelper.get("1d4")
                else -> Globals.debug("action does not modify attack preconditions: $action")
            }
        }

        for (running in runningEffectList) {
            val effect = running.effect.getTargetEffect()

            if (effect.savePenalty.isNotEmpty() && running.effect is FeatWithDuration) { // TODO: would this also work for spells ?
                Globals.debug("adding CC savePenalty  = "+effect.savePenalty)

                for (penalty in effect.savePenalty) {
                    precondition.penaltyDiceToSave += DiceBlockHelper.get(penalty)
                }
            }

            // extra damage from old spells can be applied independently (does not depend on "currentSpell")
            for (damage in effect.attackerExtraDamageOnHit) {
                precondition.bonusDamageDice += DiceBlockHelper.get (damage)
            }

            if (currentSpell != null) {
                // translate spell save effects to Preconditions
                val saveAbility = currentSpell.getSpellSaveAbility()
                val priorEffect = running.effect.getTargetEffect()

                precondition.autoFailSave = priorEffect.autoFailSave.any { it.match(saveAbility) }

                for (penalty in priorEffect.savePenalty)
                {
                    val filterMatch = priorEffect.savePenaltyFilter.any { it.match(saveAbility) }
                    if (filterMatch) {
                        precondition.penaltyDiceToSave += DiceBlockHelper.get(penalty)
                    }
                }
            }
        }

        return precondition
    }
}
