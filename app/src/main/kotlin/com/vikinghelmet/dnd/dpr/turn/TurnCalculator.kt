package com.vikinghelmet.dnd.dpr.turn

import com.vikinghelmet.dnd.dpr.character.Character
import com.vikinghelmet.dnd.dpr.util.Globals

class TurnCalculator(
    val turns: List<Turn> = ArrayList(),
    var character: Character,
    val effectManager: EffectManager
) {
    fun calculateDPRForAllTurns(): ScenarioResult {
        var turnId = 1
        var scenarioTotalDamage = 0f
        val attackResults = ArrayList<AttackResult>()

        for (turn in turns) {
            var dpr = 0f
            var actionCount = 1
            AttackResultFormatter.header()

            for (attack in turn.attacks) {
                val resultsForAttack = calculateDPR(turnId, actionCount, turn, attack)
                for (result in resultsForAttack) {
                    dpr += result.damagePerRound.select (result.getAvgMinMaxSelection())
                }
                actionCount++

                attackResults.addAll(resultsForAttack)
            }

            AttackResultFormatter.footer(turnId, "TURN TOTAL", dpr)

            effectManager.pruneRunningSpells(turnId)
            turnId++
            scenarioTotalDamage += dpr
        }

        AttackResultFormatter.footer("", "SCENARIO TOTAL", scenarioTotalDamage)
        System.err.println()
        return ScenarioResult(attackResults, scenarioTotalDamage)
    }

    fun calculateDPR(turnId: Int, actionId: Int, turn: Turn, attack: Attack): List<AttackResult>
    {
        val monster = Globals.getMonster(attack.monster)
        if (monster == null) {
            println("monster not found: "+attack.monster)
            return emptyList()
        }

        val weapon = character.getWeapon(attack.attack)
        val spell  = Globals.getSpell(attack.attack, character.is2014())

        if (weapon == null && spell == null) {
            System.err.println()
            System.err.println("spell or weapon not found: "+attack.attack)
            System.err.println()
            System.err.println("character weapons: "+ character.getWeaponNames())
            System.err.println()
            return emptyList()
        }

        attack.preconditions = effectManager.getPreconditions(turnId, actionId, turn, spell)

        val dpr = DamagePerRound(character)

        if (weapon != null) {
            val meleeOrRangeAttack = MeleeOrRangeAttack(character, null, weapon)
            val attackResult = dpr.getMeleeOrRangeDPR (meleeOrRangeAttack, attack, monster, effectManager)

            attackResult.output(character, monster, attack, turnId, actionId, weapon, effectManager)

            effectManager.pruneSpellsWaitingForNextAttack(null)
            return listOf(attackResult)
        }

        if (spell == null) return emptyList() // should not get here due to if(w/s) above; this is just to make the compiler happy

        val resultList = mutableListOf<AttackResult>()
        var effectCount = 1
        for (spellAttack in spell.getSpellAttacks()) {
            val attackResult = dpr.getSpellDPR(spellAttack, spell, attack, monster, character, effectManager)

            spell.postProcessEffectsOfOldSpells(effectManager.getRunningSpells(), attackResult)

            attackResult.output(character, monster, attack, turnId, actionId, effectCount++, spellAttack, effectManager)

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
