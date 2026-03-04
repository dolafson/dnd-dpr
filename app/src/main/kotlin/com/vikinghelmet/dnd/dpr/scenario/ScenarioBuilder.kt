package com.vikinghelmet.dnd.dpr.scenario

import com.vikinghelmet.dnd.dpr.character.Character
import com.vikinghelmet.dnd.dpr.spells.SpellHelper
import com.vikinghelmet.dnd.dpr.turn.ActionsAvailable
import com.vikinghelmet.dnd.dpr.turn.Attack
import com.vikinghelmet.dnd.dpr.turn.Turn
import com.vikinghelmet.dnd.dpr.turn.TurnCalculator
import com.vikinghelmet.dnd.dpr.util.Globals

class ScenarioBuilder(val character: Character, val monster: String) {

    fun possibleTurns(actionsAvailable: ActionsAvailable, isMelee: Boolean): List<Turn> {
        val actionList = actionsAvailable.getFullList(isMelee)
        val bonusActions = SpellHelper.getSpellNames(character.getPreparedBonusActionSpells(isMelee))
        val turnOptions = ArrayList<Turn>()

        for (action in actionList) {
            if (action.spell != null) { // generally spell attacks do not get bonus actions
                turnOptions.add(Turn(attacks = listOf(Attack(monster = monster, attack = action.getName()))))
                continue
            }
            if (action.weapon == null) {
                Globals.debug("error: action without a spell or a weapon")
                continue
            }

            // remainder pertains to weapon attacks
            val w1 = action.weapon
            var turn: Turn? = null

            // light weapon ?  see if you have a 2nd one to use in a BA
            if (w1.isLight()) {
                for (w2 in character.getWeaponList()) {
                    if (w1 == w2) continue // if you have 2 shortswords, hopefully they vary by nickname ...
                    if (w2.isLight()) {
                        turn = Turn(attacks = listOf(
                            /*
                            Attack(monster = monster, attack = (w1.nickname ?: w1.name)),
                            Attack(monster = monster, attack = (w2.nickname ?: w2.name), isBonusAction = true),
                             */
                            Attack(monster = monster, attack = w1.name),
                            Attack(monster = monster, attack = w2.name, isBonusAction = true),
                        ))
                        break
                    }
                }
            }

            // if you didn't find a 2nd light weapon, just use the first weapon w/out a BA
            if (turn == null) {
                turn = Turn(attacks = listOf(Attack(monster = monster, attack = action.getName())))
            }
            turnOptions.add(turn)

            // bonus action spells: mostly for ranger and paladin
            for (bonus in bonusActions) {
                turnOptions.add(
                    Turn(attacks = listOf(
                    //Attack(monster = monster, attack = (w1.nickname ?: w1.name)),
                    Attack(monster = monster, attack = w1.name),
                    Attack(monster = monster, attack = bonus),
                ))
                )
            }
        }
        return turnOptions
    }

    fun testPossibleTurns() {
        val actionsAvailable = character.getActionsAvailable()

        println("\n# MELEE ATTACKS\n")
        for (turn in possibleTurns(actionsAvailable, true))  println("\t "+turn.shortString())

        println("\n# RANGED ATTACKS\n")
        for (turn in possibleTurns(actionsAvailable, false))  println("\t "+turn.shortString())
        println()
    }

    fun isAttackValidForScenario(proposedAttack: Attack, currentScenario: Scenario): Boolean
    {
        val spell = Globals.getSpell(proposedAttack.attack, character.is2014())
        if (spell == null) return true // not a spell

        if (currentScenario.getSpellsAcrossTurns().isEmpty()) { // no spells used, no conflict, just add it
            return true
        }

        // if the last spell is still running, and new spell requires concentration, do not cast it ... TODO: refine this logic
        val lastSpell = currentScenario.getSpellsAcrossTurns().last()
        if ((lastSpell.getDuration() ?: 0) > 6 && spell.properties.Concentration == "Yes") {
//                    println("last spell = "+lastSpell.name+" dur = "+lastSpell.getDuration()
//                            +"skip spell: "+spell.name+", conc = "+spell.properties.Concentration)
            return false
        }
        if (!currentScenario.isSlotAvailable(spell)) {
            // println("no slots available, skip: "+spell.name)
            return false
        }

        return true
    }

    // for each spell in the proposed turn, check if it would conflict with a prior running spell,
    // due to either (a) concentration required, or (b) insufficient remaining spell slots ...
    // if either of these are true, abandon this turn option
    fun addTurnToScenarioIfValid(proposedTurn: Turn, currentScenario: Scenario): Scenario?
    {
        for (a in proposedTurn.attacks) {
            if (! isAttackValidForScenario(a, currentScenario)) return null
        }
        return Scenario (character, currentScenario.turns.map { it.copy() } + proposedTurn)
    }

    fun buildScenarios(rounds: Int, turnOptions: List<Turn>, currentScenario: Scenario, scenarioList: ArrayList<Scenario>) {
        if (rounds == 0) {
            scenarioList.add(currentScenario)
            return
        }

        for (turn in turnOptions) {
            val nextScenario = addTurnToScenarioIfValid(turn, currentScenario) ?: return
            buildScenarios(rounds - 1, turnOptions, nextScenario, scenarioList) // note: recursion
        }
    }

    fun testScenarios() {
        val actionsAvailable = character.getActionsAvailable()
        val meleeTurnOptions = possibleTurns(actionsAvailable, true)
        val rangedTurnOptions = possibleTurns(actionsAvailable, false)

        val meleeScenarioList = ArrayList<Scenario>()
        buildScenarios(6, meleeTurnOptions, Scenario(character, emptyList()), meleeScenarioList)
        println("melee: scenarioList size = "+meleeScenarioList.size)

        val rangedScenarioList = ArrayList<Scenario>()
        buildScenarios(6, rangedTurnOptions, Scenario(character, emptyList()), rangedScenarioList)
        println("ranged: scenarioList size = "+rangedScenarioList.size)
    }

    fun runScenarios(isMelee: Boolean) {
        val actionsAvailable = character.getActionsAvailable()
        val turnOptions = possibleTurns(actionsAvailable, isMelee)
        val scenarioList = ArrayList<Scenario>()
        buildScenarios(6, turnOptions, Scenario(character, emptyList()), scenarioList)

        val resultList = ArrayList<ScenarioResult>()
        for (scenario in scenarioList) {
            val scenarioResult = TurnCalculator(scenario).calculateDPRForAllTurns()
            resultList.add(scenarioResult)
        }

        val sortedResults = resultList.sortedByDescending { it.totalDPR }
        for (scenarioResult in sortedResults) {
            System.err.println(String.format("%2.2f \t%s", scenarioResult.totalDPR, scenarioResult.scenario.getLabel()))
            scenarioResult.output()
        }
    }

}