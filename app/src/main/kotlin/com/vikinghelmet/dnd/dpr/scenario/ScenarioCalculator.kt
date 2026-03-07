package com.vikinghelmet.dnd.dpr.scenario

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
        val weapon = scenario.character.getWeapon(attack.attack)
        val spell  = Globals.getSpell(attack.attack, scenario.character.is2014())

        if (weapon == null && spell == null) {
            System.err.println()
            System.err.println("spell or weapon not found: "+attack.attack)
            System.err.println()
            System.err.println("character weapons: "+ scenario.character.getWeaponNames())
            System.err.println()
            return emptyList()
        }

        attack.preconditions = effectManager.getPreconditions(attack, turnId, actionId, turn, spell)

        val dpr = DamagePerRound(scenario.character)

        if (weapon != null) {
            val attackResult = dpr.getMeleeOrRangeDPR (weapon, attack, attack.monster, effectManager)

            attackResult.update(scenario.character, attack, turnId, actionId, 1, weapon, null, effectManager.toString())

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
