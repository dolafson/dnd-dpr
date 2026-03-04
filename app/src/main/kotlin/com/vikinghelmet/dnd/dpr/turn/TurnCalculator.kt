package com.vikinghelmet.dnd.dpr.turn

import com.vikinghelmet.dnd.dpr.character.Character
import com.vikinghelmet.dnd.dpr.util.Globals

class TurnCalculator(
    val turns: ArrayList<Turn> = ArrayList(),
    var character: Character
) {
    fun calculateDPRForAllTurns() {
        var turnId = 1
        var scenarioTotalDamage = 0f

        for (turn in turns) {
            var dpr = 0f
            var actionCount = 1
            AttackResultFormatter.header()

            for (attack in turn.attacks) {
                val resultList = calculateDPR(turnId, actionCount, turn, attack)
                for (result in resultList) {
                    dpr += result.damagePerRound.select (result.getAvgMinMaxSelection())
                }
                actionCount++
            }

            AttackResultFormatter.footer(turnId, "TURN TOTAL", dpr)

            EffectManager.pruneRunningSpells(turnId)
            turnId++
            scenarioTotalDamage += dpr
        }

        AttackResultFormatter.footer("", "SCENARIO TOTAL", scenarioTotalDamage)
        System.err.println()
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

        attack.preconditions = EffectManager.getPreconditions(turnId, actionId, turn, spell)

        val dpr = DamagePerRound(character)

        if (weapon != null) {
            val meleeOrRangeAttack = MeleeOrRangeAttack(character, null, weapon)
            val attackResult = dpr.getMeleeOrRangeDPR (meleeOrRangeAttack, attack, monster)

            attackResult.output(character, monster, attack, turnId, actionId, weapon)

            EffectManager.pruneSpellsWaitingForNextAttack(null)
            return listOf(attackResult)
        }

        if (spell == null) return emptyList() // should not get here due to if(w/s) above; this is just to make the compiler happy

        val resultList = mutableListOf<AttackResult>()
        var effectCount = 1
        for (spellAttack in spell.getSpellAttacks()) {
            val attackResult = dpr.getSpellDPR(spellAttack, spell, attack, monster, character)

            spell.postProcessEffectsOfOldSpells(EffectManager.getRunningSpells(), attackResult)

            attackResult.output(character, monster, attack, turnId, actionId, effectCount++, spellAttack)

            EffectManager.pruneSpellsWaitingForNextAttack(spellAttack) // do this pruning before adding current spell to the manager (below)
            if (attackResult.targetHadDisadvantageOnSave == true) {

            }

            resultList.add(attackResult)
        }

        if (!spell.getTargetEffect().isEmpty()) { // we only track spells with a non-empty effect
            EffectManager.add(turnId, spell)
            Globals.debug("adding to running list: "+spell.name)
        }

        return resultList
    }
}
