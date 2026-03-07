package com.vikinghelmet.dnd.dpr.scenario

import com.vikinghelmet.dnd.dpr.character.inventory.Weapon
import com.vikinghelmet.dnd.dpr.spells.Spell
import com.vikinghelmet.dnd.dpr.turn.Attack
import com.vikinghelmet.dnd.dpr.turn.AttackResult
import com.vikinghelmet.dnd.dpr.turn.DamagePerRound
import com.vikinghelmet.dnd.dpr.turn.Turn
import com.vikinghelmet.dnd.dpr.util.Globals

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
                val resultsForAttack = calculateDPR(turnId, actionCount, turn, attack)
                for (result in resultsForAttack) {
                    dpr += result.damagePerRound.select (result.getAvgMinMaxSelection())
                }
                actionCount++

                attackResults.addAll(resultsForAttack)
            }

            effectManager.pruneRunningSpells(turnId)
            turnId++
            scenarioTotalDamage += dpr
        }

        return ScenarioResult(scenario, attackResults, scenarioTotalDamage)
    }

    fun calculateDPR(turnId: Int, actionId: Int, turn: Turn, attack: Attack): List<AttackResult>
    {
        val spell = if (attack.action is Spell) attack.action else null
        attack.preconditions = effectManager.getPreconditions(attack, turnId, actionId, turn, spell)

        val dpr = DamagePerRound(scenario.character)

        if (attack.action is Weapon) {
            val attackResult = dpr.getMeleeOrRangeDPR (attack.action, attack, attack.monster, effectManager)

            attackResult.update(scenario.character, attack, turnId, actionId, 1, attack.action, null, effectManager.toString())

            effectManager.pruneSpellsWaitingForNextAttack(null)
            return listOf(attackResult)
        }

        if (spell == null) return emptyList() // should not get here due to if(w/s) above; this is just to make the compiler happy

        val resultList = mutableListOf<AttackResult>()
        var effectCount = 1
        for (spellAttack in spell.getSpellAttacks()) {
            val attackResult = dpr.getSpellDPR(spellAttack, spell, attack, attack.monster, scenario.character, effectManager)

            spell.postProcessEffectsOfOldSpells(effectManager.getRunningSpells(), attackResult)

            attackResult.update(
                scenario.character,
                attack,
                turnId,
                actionId,
                effectCount++,
                null,
                spellAttack,
                effectManager.toString()
            )

            effectManager.pruneSpellsWaitingForNextAttack(spellAttack) // do this pruning before adding current spell to the effectManager (below)
            resultList.add(attackResult)
        }

        if (!spell.getTargetEffect().isEmpty()) { // we only track spells with a non-empty effect
            effectManager.add(turnId, spell)
            Globals.debug("adding to running list: "+spell.name)
        }

        return resultList
    }
}
