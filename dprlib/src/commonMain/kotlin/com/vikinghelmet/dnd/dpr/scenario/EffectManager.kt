package com.vikinghelmet.dnd.dpr.scenario

import com.vikinghelmet.dnd.dpr.character.actions.ActionModifier
import com.vikinghelmet.dnd.dpr.spells.Spell
import com.vikinghelmet.dnd.dpr.spells.SpellAttack
import com.vikinghelmet.dnd.dpr.turn.Attack
import com.vikinghelmet.dnd.dpr.turn.Preconditions
import com.vikinghelmet.dnd.dpr.util.DiceBlockHelper
import com.vikinghelmet.dnd.dpr.util.Globals
import com.vikinghelmet.dnd.dpr.util.TargetEffect


data class EffectManager(val runningEffectList: ArrayList<TargetEffect>)
{
    var assumeSpellEffectSuccess = true

    fun attackerHasAdvantage() = runningEffectList.any { it.attackerHasAdvantage == true }
    fun isAutoCrit() = assumeSpellEffectSuccess && runningEffectList.any { it.attackerAutoCrit == true }


    override fun toString(): String {
        val buf = StringBuilder()
        for (running in runningEffectList) buf.append(running)
        return buf.toString()
    }

    fun add(effect: TargetEffect) {
        if (effect.isEmpty()) {
            return // we only track spells with a non-empty effect
        }

        for (runningEffect in runningEffectList) {
            if (runningEffect.cause == effect.cause) {
                Globals.debug("no double dipping on effect cause")
                return
            }
        }

        runningEffectList.add(effect)
        Globals.debug("adding to running list: "+effect)
    }

    fun pruneEffectsAtEndOfTurn(turnId: Int) {
        val iterator = runningEffectList.listIterator()
        while (iterator.hasNext()) {
            val running  = iterator.next()
            val deltaT   = turnId - running.startTurn

            if (deltaT >= running.getDuration()) {
                Globals.debug("effect is complete, remove from running list: "+running.toString())
                iterator.remove()
            }
        }
    }

    fun pruneEffectsWaitingForNextAttack(spellAttack: SpellAttack?) {
        val iterator = runningEffectList.listIterator()
        while (iterator.hasNext()) {
            val running = iterator.next()

            val causedBySpell = running.getSpell()
            val oneShot = causedBySpell == null || running.getDuration() <= 1 // TODO: better way ?
            if (!oneShot) continue

            if (running.hasSaveImpact() &&
                spellAttack != null && spellAttack.isSavingThrowAttack())
            {
                Globals.debug("effect was waiting for next saving throw, removing it from running list: "+running.toString())
                iterator.remove()
            }

            if (causedBySpell != null && causedBySpell.description.contains("next attack") &&
                (spellAttack == null || spellAttack.isMeleeOrRangeAttack()))
            {
                Globals.debug("effect was waiting for next melee/range attack, removing it from running list: "+running.toString())
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

        for (priorEffect in runningEffectList)
        {
            if (priorEffect.probability < 100f && !assumeSpellEffectSuccess) {
                continue
            }

            for (penalty in priorEffect.savePenalty) {
                precondition.penaltyDiceToSave += DiceBlockHelper.get(penalty)
            }

            for (damage in priorEffect.attackerExtraDamageOnHit) {
                precondition.bonusDamageDice += DiceBlockHelper.get (damage)
            }

            if (currentSpell != null) {
                // translate spell save effects to Preconditions
                val saveAbility = currentSpell.getSpellSaveAbility()
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

    /*
        var bonusDiceToSave: DiceBlock = DiceBlock(0, 0, 0, 0, 0),      // not used yet
        var penaltyDiceToSave: DiceBlock = DiceBlock(0, 0, 0, 0, 0),    // used

        var bonusDiceToHit: DiceBlock = DiceBlock(0, 0, 0, 0, 0),       // not used yet
        var penaltyDiceToHit: DiceBlock = DiceBlock(0, 0, 0, 0, 0),     // not used yet

        var bonusDamageDice: DiceBlock = DiceBlock(0, 0, 0, 0, 0),      // used
        var autoFailSave: Boolean = false,                              // used
     */
}
