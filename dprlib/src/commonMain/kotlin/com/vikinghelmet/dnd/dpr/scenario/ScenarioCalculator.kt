package com.vikinghelmet.dnd.dpr.scenario

import com.vikinghelmet.dnd.dpr.character.feats.Feat
import com.vikinghelmet.dnd.dpr.character.feats.FeatWithDuration
import com.vikinghelmet.dnd.dpr.character.inventory.Weapon
import com.vikinghelmet.dnd.dpr.spells.Spell
import com.vikinghelmet.dnd.dpr.spells.SpellAttack
import com.vikinghelmet.dnd.dpr.turn.ActionCalculator
import com.vikinghelmet.dnd.dpr.turn.Attack
import com.vikinghelmet.dnd.dpr.turn.AttackResult
import com.vikinghelmet.dnd.dpr.util.Globals
import com.vikinghelmet.dnd.dpr.util.TargetEffect

class ScenarioCalculator(
    val scenario: Scenario,    
    val effectManager: EffectManager = EffectManager(ArrayList())
)
{
    fun calculateDPRForAllTurns(): ScenarioResult {
        var turnId = 1
        var scenarioTotalDamage = 0f
        val attackResults = ArrayList<AttackResult>()

        for (turn in scenario.turns) {
            var dpr = 0f
            var actionCount = 1

            for (attack in turn.attacks) {
                val resultsForAttack = calculateDPR(turnId, actionCount, attack)
                for (result in resultsForAttack) {
                    dpr += result.damagePerRound.select (result.getAvgMinMaxSelection())

                    val fullEffect = result.damageFullEffect.select (result.getAvgMinMaxSelection())
                    scenarioTotalDamage += fullEffect
                }
                actionCount++

                attackResults.addAll(resultsForAttack)
            }

            effectManager.pruneRunningSpells(turnId)
            turnId++
        }

        return ScenarioResult(scenario, attackResults, scenarioTotalDamage)
    }

    fun calculateDPR(turnId: Int, actionId: Int, attack: Attack): List<AttackResult>
    {
        val spell = if (attack.action is Spell) attack.action else null
        attack.preconditions = effectManager.getPreconditions(attack, spell)

        val dpr = ActionCalculator(scenario, effectManager)

        if (attack.action is Weapon) {
            val attackResult = dpr.getMeleeOrRangeDPR (attack.action, attack)

            attackResult.update(turnId, actionId, 1)

            if (scenario.character.isFeatEnabled(Feat.ColdCaster.getNameWithWS())) {
                // TODO: must also check if damage type = Cold (though WW always adds cold damage to weapons)
                effectManager.add(turnId,
                    FeatWithDuration(Feat.ColdCaster, 1,
                        TargetEffect(savePenalty = mutableListOf("1d4"))))

                Globals.debug("after adding CC feat, effects = "+effectManager)
            }

            effectManager.pruneSpellsWaitingForNextAttack(null)
            return listOf(attackResult)
        }

        if (spell == null) return emptyList() // should not get here due to if(w/s) above; this is just to make the compiler happy

        val resultList = mutableListOf<AttackResult>()
        var effectCount = 1

        for (spellAttack in spell.getSpellAttacks()) {
            // if there is nothing special going on, simply process the spell and collect its results
            if (effectManager.runningEffectList.isEmpty() || spellAttack.getNumTargetsAffected(scenario) <= 1) {
                resultList.add (processSpellAttack (dpr, spellAttack, spell, attack, turnId, actionId, effectCount++))
                continue
            }

            // this weird bit of logic is needed for AreaOfEffect spells, which hit multiple targets
            // one of the targets may have a save penalty, while others may not
            // to handle that, break them apart into separate spellAttacks, with varying conditions ...
            val copyMinusOne = SpellAttack(spellAttack, scenario)
            resultList.add (processSpellAttack (dpr, spellAttack, spell, attack, turnId, actionId, effectCount++))
            resultList.add (processSpellAttack (dpr, copyMinusOne, spell, attack, turnId, actionId, effectCount++))
        }

        if (!spell.getTargetEffect().isEmpty()) { // we only track spells with a non-empty effect
            effectManager.add(turnId, spell)
            Globals.debug("adding to running list: "+spell.name)
        }

        return resultList
    }

    private fun processSpellAttack(
        dpr: ActionCalculator,
        spellAttack: SpellAttack,
        spell: Spell,
        attack: Attack,
        turnId: Int,
        actionId: Int,
        effectCount: Int
    ): AttackResult {
        // preconditions need to be computed each time, as they may vary between initial spell attack and subsequent spell effects
        attack.preconditions = effectManager.getPreconditions(attack, spell)

        val attackResult = dpr.getSpellDPR(spellAttack, spell, attack)

        spell.postProcessEffectsOfOldSpells(effectManager.getRunningSpells(), attackResult)

        attackResult.update(turnId, actionId, effectCount, spellAttack)

        effectManager.pruneSpellsWaitingForNextAttack(spellAttack) // do this pruning before adding current spell to the effectManager (below)
        return attackResult
    }
}
