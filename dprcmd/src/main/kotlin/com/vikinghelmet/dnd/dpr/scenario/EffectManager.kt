package com.vikinghelmet.dnd.dpr.scenario

import com.vikinghelmet.dnd.dpr.character.actions.ActionModifier
import com.vikinghelmet.dnd.dpr.character.feats.FeatWithDuration
import com.vikinghelmet.dnd.dpr.spells.Spell
import com.vikinghelmet.dnd.dpr.spells.SpellAttack
import com.vikinghelmet.dnd.dpr.turn.Attack
import com.vikinghelmet.dnd.dpr.turn.Preconditions
import com.vikinghelmet.dnd.dprlib.util.DiceBlockHelper
import com.vikinghelmet.dnd.dpr.util.Globals
import com.vikinghelmet.dnd.dpr.util.TargetEffect

interface EffectWithDuration {
    fun getDuration(): Int?
    fun getTargetEffect(): TargetEffect
    fun appliesEffectToNextTargetSaveOnly(): Boolean
    fun appliesToNextMeleeOrRangeAttackOnly(): Boolean
}

data class RunningEffect (val startTurn: Int, val effect: EffectWithDuration)

data class EffectManager(
    val runningEffectList: ArrayList<RunningEffect>
)
{
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

    fun getRunningSpells(): List<Spell> {
        return runningEffectList.filter { it.effect is Spell }.map { it.effect as Spell }
    }

    fun pruneRunningSpells(turnId: Int) {
        val iterator = runningEffectList.listIterator()
        while (iterator.hasNext()) {
            val running = iterator.next()
            val deltaT = turnId - running.startTurn
            val effect = running.effect
            val duration = effect.getDuration() ?: 0
            if (deltaT >= duration) {
                Globals.debug("spell is complete, remove from running list: "+effect.toString())
                iterator.remove()
            }
        }
    }

    fun pruneSpellsWaitingForNextAttack(spellAttack: SpellAttack?) {
        val iterator = runningEffectList.listIterator()
        while (iterator.hasNext()) {
            val running = iterator.next()

            if (running.effect.appliesEffectToNextTargetSaveOnly() && spellAttack != null && spellAttack.isSavingThrowAttack()) {
                Globals.debug("spell was waiting for next saving throw, removing it from running list: "+running.effect.toString())
                iterator.remove()
            }

            if (running.effect.appliesToNextMeleeOrRangeAttackOnly() && (spellAttack == null || spellAttack.isMeleeOrRangeAttack())) {
                Globals.debug("spell was waiting for next melee/range attack, removing it from running list: "+running.effect.toString())
                iterator.remove()
            }
        }
    }

    fun getPreconditions(attack: Attack, currentSpell: Spell?): Preconditions? {
        val precondition = Preconditions()
        precondition.bonusDamageDice = DiceBlockHelper.emptyBlock()
        precondition.penaltyDiceToSave = DiceBlockHelper.emptyBlock()

        for (action in attack.actionModifiers) {
            when (action) {
                ActionModifier.ColossusSlayer -> precondition.bonusDamageDice!! += DiceBlockHelper.get("1d8")
                ActionModifier.DreadfulStrike -> precondition.bonusDamageDice!! += DiceBlockHelper.get("2d6")
                ActionModifier.PolarStrikes   -> precondition.bonusDamageDice!! += DiceBlockHelper.get("1d4")
                else -> Globals.debug("action does not modify attack preconditions: $action")
            }
        }

        for (running in runningEffectList) {
            val effect = running.effect.getTargetEffect()

            if (running.effect is FeatWithDuration) {
                Globals.debug("getPreconditions: running effect is a feat: "+running.effect)
            }

            if (effect.savePenalty.isNotEmpty() && running.effect is FeatWithDuration) { // TODO: would this also work for spells ?
                Globals.debug("adding CC savePenalty  = "+effect.savePenalty)

                for (penalty in effect.savePenalty) {
                    precondition.penaltyDiceToSave!! += DiceBlockHelper.get(penalty)
                }
            }

            // extra damage from old spells can be applied independently (does not depend on "currentSpell")
            for (damage in effect.attackerExtraDamageOnHit) {
//                precondition.bonusDamageDice = precondition.bonusDamageDice!!.add (DiceBlockHelper.getDiceBlock (damage))
                precondition.bonusDamageDice!! += DiceBlockHelper.get (damage)
            }

            if (currentSpell != null) {
                currentSpell.preProcessEffectsOfOldSpell (running.effect, precondition)
            }
        }
        return precondition
    }

    fun attackerHasAdvantage(): Boolean {
        for (running in runningEffectList) {
            if (running.effect.getTargetEffect().attackerHasAdvantage == true) return true
        }
        return false
    }

    fun isAutoCrit(): Boolean {
        for (running in runningEffectList) {
            if (running.effect.getTargetEffect().attackerAutoCrit == true) return true
        }
        return false
    }
    override fun toString(): String {
        val buf = StringBuilder()
        for (running in runningEffectList) buf.append(running.effect.getTargetEffect())
        return buf.toString()
    }
}
