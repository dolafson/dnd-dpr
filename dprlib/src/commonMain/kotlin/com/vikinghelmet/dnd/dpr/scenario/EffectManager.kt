package com.vikinghelmet.dnd.dpr.scenario

import com.vikinghelmet.dnd.dpr.character.actions.ActionModifier
import com.vikinghelmet.dnd.dpr.character.inventory.MasteryProperty
import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import com.vikinghelmet.dnd.dpr.spells.Spell
import com.vikinghelmet.dnd.dpr.spells.SpellAttack
import com.vikinghelmet.dnd.dpr.turn.Attack
import com.vikinghelmet.dnd.dpr.turn.Preconditions
import com.vikinghelmet.dnd.dpr.util.DiceBlockHelper
import com.vikinghelmet.dnd.dpr.util.Globals
import com.vikinghelmet.dnd.dpr.util.TargetEffect


data class EffectManager(val runningEffectList: MutableList<TargetEffect>,)
{
    constructor(other: EffectManager, allowFailure: Boolean):
            this(runningEffectList = other.runningEffectList.filter { it.probability == 100f || allowFailure}.toMutableList())

    fun chanceOfSuccess(): Float {
        //return if (runningEffectList.isEmpty()) 100f else runningEffectList.minOf { it.probability }

        // prune advantage/disadvantage, as they get consumed by ActionCalculator (end of action),
        // while this function is consumed by ScenarioCalculator (end of turn)
        val filtered = runningEffectList.filter {
            it.attackerHasAdvantage != true && it.disadvantageOnSave.isEmpty()
        }
        return if (filtered.isEmpty()) 100f else filtered.minOf { it.probability }
    }

    // TODO: support multiple forms of advantage on a single turn?
    fun attackerHasAdvantage() = runningEffectList.firstOrNull { it.attackerHasAdvantage == true }

    // TODO: support multiple forms of save disadvantage on a single turn?
    fun targetHasDisadvantageOnSave(abilityType: AbilityType?): TargetEffect? {
        return if (abilityType != null) null
            else runningEffectList.firstOrNull { it.disadvantageOnSave.any { it2 -> it2.match(abilityType) }}
    }

    fun isAutoCrit() = runningEffectList.any { it.attackerAutoCrit == true }

    override fun toString(): String {
        val buf = StringBuilder()
        for (running in runningEffectList) buf.append(running)
        return buf.toString()
    }

    fun toStringConditions(): String {
        val buf = StringBuilder()
        for (running in runningEffectList) {
            if (running.conditions.isNotEmpty()) {
                buf.append("${Globals.getPercent(running.probability*100)}% = ${ running.conditions.joinToString() }")
            }
        }
        return buf.toString()
    }

    fun add(effect: TargetEffect) {
        if (effect.isEmpty()) {
            return // we only track spells with a non-empty effect
        }

        for (runningEffect in runningEffectList) {
            // TODO: fix conflict between
            //  - "no double dip on effects that occur once per round"
            //  - "Vex continues until end of next round"
            if (runningEffect.cause == effect.cause && !(effect.cause is MasteryProperty)) {
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
