package com.vikinghelmet.dnd.dpr.scenario

import com.vikinghelmet.dnd.dpr.character.Character
import com.vikinghelmet.dnd.dpr.character.actions.ActionModifier
import com.vikinghelmet.dnd.dpr.character.inventory.Weapon
import com.vikinghelmet.dnd.dpr.monsters.Monster
import com.vikinghelmet.dnd.dpr.spells.Spell
import com.vikinghelmet.dnd.dpr.spells.SpellHelper
import com.vikinghelmet.dnd.dpr.turn.Attack
import com.vikinghelmet.dnd.dpr.turn.Turn
import com.vikinghelmet.dnd.dpr.util.Constants
import com.vikinghelmet.dnd.dpr.util.Globals

class ScenarioBuilder(val character: Character, val monster: Monster) {

    fun possibleTurns(actionsAvailable: ActionsAvailable, isMelee: Boolean): List<Turn> {
        val actionList = actionsAvailable.getFullList(isMelee)
        val bonusActionNames = SpellHelper.getSpellNames(character.getPreparedBonusActionSpells(isMelee))
        val turnOptions = ArrayList<Turn>()

        for (action in actionList) {
            if (action is Spell) { // generally spell attacks do not get bonus actions
                turnOptions.add(Turn(attacks = listOf(Attack(monster = monster, action = action))))
                continue
            }
            if (action !is Weapon) {
                Globals.debug("error: action without a spell or a weapon")
                continue
            }

            // remainder pertains to weapon attacks
            var turn: Turn? = null

            // light weapon ?  see if you have a 2nd one to use in a BA
            if (action.isLight()) {
                for (w2 in character.getWeaponList()) {
                    if (action == w2) continue // if you have 2 shortswords, hopefully they vary by nickname ...
                    if (w2.isLight()) {
                        turn = Turn(
                            attacks = listOf(
                                /*
                                Attack(monster = monster, attack = (w1.nickname ?: w1.name)),
                                Attack(monster = monster, attack = (w2.nickname ?: w2.name), isBonusAction = true),
                                 */
                                Attack(monster = monster, action = action),
                                Attack(monster = monster, action = w2, isBonusAction = true),
                            )
                        )
                        break
                    }
                }
            }

            // if you didn't find a 2nd light weapon, just use the first weapon w/out a BA
            if (turn == null) {
                turn = Turn(attacks = listOf(Attack(monster = monster, action = action)))
            }
            turnOptions.add(turn)

            // bonus action spells: mostly for ranger and paladin
            for (bonusName in bonusActionNames) {
                val bonus = Globals.getSpell(bonusName, character.is2014())
                turnOptions.add(
                    Turn(
                        attacks = listOf(
                        //Attack(monster = monster, attack = (w1.nickname ?: w1.name)),
                        Attack(monster = monster, action = action),
                        Attack(monster = monster, action = bonus, isBonusAction = true),
                    )
                    ))
            }
        }
        return turnOptions
    }

    fun isAttackValidForScenario(proposedAttack: Attack, currentScenario: Scenario): Boolean
    {
        if (proposedAttack.action !is Spell) return true
        val spell = proposedAttack.action

        if (currentScenario.getSpellsAcrossTurns().isEmpty()) { // no spells used, no conflict, just add it
            return true
        }

        // if the last spell is still running, and new spell requires concentration, do not cast it ... TODO: refine this logic
        val lastSpell = currentScenario.getSpellsAcrossTurns().last()

        // System.err.println("lastSpell = $lastSpell, new spell = "+proposedAttack.action.name)

        var iceAfterMark=false
        if (proposedAttack.action.name == "Ice Knife" && lastSpell.name == "Hunter's Mark") {
            Globals.debug("ice after mark")
            iceAfterMark=true
        }

        if ((lastSpell.getDuration() ?: 0) > Constants.DEFAULT_NUM_TURNS_PER_SCENARIO && spell.properties.Concentration == "Yes") {
            if (iceAfterMark) {
                Globals.debug("last spell = "+lastSpell.name+" dur = "+lastSpell.getDuration()
                            +"skip spell: "+spell.name+", conc = "+spell.properties.Concentration)
            }
            return false
        }
        if (!currentScenario.isSlotAvailable(spell)) {
            if (iceAfterMark) Globals.debug("no slots available, skip: "+spell.name)
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
            if (a.action is Spell) {
                val lastSpell = currentScenario.getSpellsAcrossTurns().lastOrNull()
                //System.err.println("lastSpell = $lastSpell, proposed spell = " + a.action.name)
            }

            if (! isAttackValidForScenario(a, currentScenario)) return null
        }

        // make a deep copy of proposed turn, as turn can be modified later (when we add action mods)
        val copy = Turn (proposedTurn.attacks.map {
            a -> Attack(a.monster, a.action, ArrayList(), null, a.isBonusAction)
        }.toMutableList())

        return Scenario (character, currentScenario.turns.map { it.copy() } + copy)
    }

    fun buildScenarios(rounds: Int, turnOptions: List<Turn>, currentScenario: Scenario, scenarioList: ArrayList<Scenario>) {
        if (rounds == 0) {
            scenarioList.add(currentScenario)
            return
        }
/*
        val lastSpell = currentScenario.getSpellsAcrossTurns().lastOrNull()
        if (lastSpell != null) {
            val turnLabels = turnOptions.map { t -> t.attacks.map { a -> a.action.toString() } }
            System.err.println("lastSpell = $lastSpell, turnOptions = " + turnLabels)
        }
*/
        for (turn in turnOptions) {
/*
            if (lastSpell != null) {
                val turnLabels = turn.attacks.map { a -> a.action.toString() }
                System.err.println("lastSpell = $lastSpell, turn = " + turnLabels)
            }
*/
            val nextScenario = addTurnToScenarioIfValid(turn, currentScenario) ?: continue // if this one didn't work, try the next option

            buildScenarios(rounds - 1, turnOptions, nextScenario, scenarioList) // note: recursion
        }
    }

    fun countModifier(mod: ActionModifier, scenario: Scenario): Int {
        var result = 0
        for (turn in scenario.turns) for (a in turn.attacks) if (a.actionModifiers.contains(mod)) result++
        return result
    }

    fun isActionModifierValidForTurn(mod: ActionModifier, scenario: Scenario, turnId: Int, turn: Turn, attack: Attack): Boolean
    {
        if (attack.action !is Weapon) {
            Globals.debug("for now, all modifiers apply only to weapon attacks")
            return false
        }

        for (a in turn.attacks) {
            if (a.actionModifiers.contains(mod)) {
                Globals.debug("for now, all modifiers may be used only once per turn")
                return false
            }
        }

        when (mod) {
            ActionModifier.ColossusSlayer -> {
                // weapon attacks only
                // at most once per turn
                // only applies if creature is missing HP
                // - for now, only alternating turns (1,3,...) to simulate changing to a fresh target
                val odd = ((turnId % 2) != 0)
                return odd
            }
            ActionModifier.DreadfulStrike -> {
                // weapon attacks only
                // at most once per turn
                // limited use per day, similar to hunter's mark (tied to WIS bonus)
                return countModifier(mod, scenario) < character.getSpellAbilityBonusWithoutPB()
            }
            ActionModifier.PolarStrikes   -> {
                // weapon attacks only
                // at most once per turn
                return true
            }
            else -> Globals.debug("action does not modify attack preconditions: $mod")
        }
        return false
    }

    fun addActionModifiers(scenarioList: List<Scenario>) {
        val actionModifiersAvailable = character.getActionModifiersAvailable()
        //System.err.println("modifiers available=$actionModifiersAvailable")

        for (scenario in scenarioList) {
            var turnId = 0
            for (turn in scenario.turns) {
                for (attack in turn.attacks) {
                    for (mod in actionModifiersAvailable) {
                        val isValid = isActionModifierValidForTurn (mod, scenario, turnId, turn, attack)
                        // System.err.println("turnId=$turnId, mod=$mod, isValid=$isValid")
                        if (isValid) {
                            attack.actionModifiers.add(mod)
                        }
                    }
                }
                turnId++
            }
        }
    }

    fun runScenarios(isMelee: Boolean) {
        val actionsAvailable = character.getActionsAvailable()
        val turnOptions = possibleTurns(actionsAvailable, isMelee)
        val scenarioList = ArrayList<Scenario>()
        buildScenarios(Constants.DEFAULT_NUM_TURNS_PER_SCENARIO, turnOptions, Scenario(character, emptyList()), scenarioList)

        addActionModifiers(scenarioList)
/*
        for (scenario in scenarioList) for (turnId in scenario.turns.indices) for (actionId in scenario.turns[turnId].attacks.indices) {
            //for (turn in scenario.turns) for (a in turn.attacks) {
            val mods = scenario.turns[turnId].attacks[actionId].actionModifiers
            System.err.println("turnId=$turnId, actionId=$actionId, mods=$mods")
        }
*/
        val resultList = ArrayList<ScenarioResult>()
        for (scenario in scenarioList) {
            val scenarioResult = ScenarioCalculator(scenario).calculateDPRForAllTurns()
            resultList.add(scenarioResult)
        }

        val sortedResults = resultList.sortedByDescending { it.totalDPR }.take(Constants.DEFAULT_SCENARIO_OUTPUT_MAX)
        for (scenarioResult in sortedResults) {
            System.err.println(String.format("%2.2f \t%s", scenarioResult.totalDPR, scenarioResult.scenario.getLabel()))
            scenarioResult.output()
            //System.exit(0)
        }
    }

}