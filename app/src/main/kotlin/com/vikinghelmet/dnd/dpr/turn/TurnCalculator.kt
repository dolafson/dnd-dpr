package com.vikinghelmet.dnd.dpr.turn

import com.vikinghelmet.dnd.dpr.scenario.EffectManager
import com.vikinghelmet.dnd.dpr.scenario.Scenario
import com.vikinghelmet.dnd.dpr.scenario.ScenarioResult
import com.vikinghelmet.dnd.dpr.util.Globals

class TurnCalculator(
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

        return ScenarioResult(attackResults, scenarioTotalDamage)
    }

    fun calculateDPR(turnId: Int, actionId: Int, turn: Turn, attack: Attack): List<AttackResult>
    {
        val monster = Globals.getMonster(attack.monster)
        if (monster == null) {
            println("monster not found: "+attack.monster)
            return emptyList()
        }

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

        attack.preconditions = effectManager.getPreconditions(turnId, actionId, turn, spell)

        val dpr = DamagePerRound(scenario.character)

        if (weapon != null) {
            val meleeOrRangeAttack = MeleeOrRangeAttack(scenario.character, null, weapon)
            val attackResult = dpr.getMeleeOrRangeDPR (meleeOrRangeAttack, attack, monster, effectManager)

            //attackResult.output(scenario.character, monster, attack, turnId, actionId, weapon, effectManager.toString())
            attackResult.update(scenario.character, monster, attack, turnId, actionId, 1, weapon, null, effectManager.toString())

            effectManager.pruneSpellsWaitingForNextAttack(null)
            return listOf(attackResult)
        }

        if (spell == null) return emptyList() // should not get here due to if(w/s) above; this is just to make the compiler happy

        val resultList = mutableListOf<AttackResult>()
        var effectCount = 1
        for (spellAttack in spell.getSpellAttacks()) {
            val attackResult = dpr.getSpellDPR(spellAttack, spell, attack, monster, scenario.character, effectManager)

            spell.postProcessEffectsOfOldSpells(effectManager.getRunningSpells(), attackResult)

            //attackResult.output(scenar            //attackResult.output(scenario.character, monster, attack, turnId, actionId, effectCount++, spellAttack, effectManager.toString())io.character, monster, attack, turnId, actionId, effectCount++, spellAttack, effectManager.toString())
            attackResult.update(scenario.character, monster, attack, turnId, actionId, effectCount++, null, spellAttack, effectManager.toString())

            effectManager.pruneSpellsWaitingForNextAttack(spellAttack) // do this pruning before adding current spell to the manager (below)

            resultList.add(attackResult)
        }

        if (!spell.getTargetEffect().isEmpty()) { // we only track spells with a non-empty effect
            effectManager.add(turnId, spell)
            Globals.debug("adding to running list: "+spell.name)
        }

        return resultList
    }
}
