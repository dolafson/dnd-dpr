package com.vikinghelmet.dnd.dpr.scenario

import com.vikinghelmet.dnd.dpr.character.Character
import com.vikinghelmet.dnd.dpr.character.actions.ActionModifier
import com.vikinghelmet.dnd.dpr.character.inventory.Weapon
import com.vikinghelmet.dnd.dpr.character.inventory.WeaponProperty
import com.vikinghelmet.dnd.dpr.monsters.Monster
import com.vikinghelmet.dnd.dpr.spells.Spell
import com.vikinghelmet.dnd.dpr.turn.Attack
import com.vikinghelmet.dnd.dpr.turn.Turn
import com.vikinghelmet.dnd.dpr.util.Constants
import com.vikinghelmet.dnd.dpr.util.Globals
import dev.shivathapaa.logger.api.LoggerFactory
import kotlin.time.measureTime

class ScenarioBuilder(
    val character: Character,
    val monster: Monster,
    val actionsAvailable: ActionsAvailable
) {
    val logger = LoggerFactory.get(ScenarioBuilder::class.simpleName ?: "no simpleName")

    var turnOptions: MutableList<Turn> = mutableListOf()
    var scenarioList = ArrayList<Scenario>()
    var resultList: MutableList<ScenarioResult> = mutableListOf()
    var runIterator: Iterator<Scenario> = scenarioList.iterator()

    constructor(character: Character, monster: Monster) : this(character,monster,character.getActionsAvailable()) {

    }
    fun build(targetProximity: Int, numberOfTurns: Int, numTargets: Int, targetSpacing: Int) {
        turnOptions.clear()
        scenarioList.clear()
        resultList.clear()

        logDuration("possibleTurns", { turnOptions.addAll(possibleTurns(actionsAvailable, targetProximity)) })
        println("# num(possibleTurns) = ${turnOptions.size}")

        logDuration("buildScenarios", {
            buildScenarios(
                numberOfTurns,
                turnOptions,
                Scenario(character, emptyList(), numTargets, targetSpacing),
                scenarioList
            )
        })

        println("# num(scenarios) = ${scenarioList.size}")
        logDuration("addActionModifiers", { addActionModifiers(scenarioList) })

        runIterator = scenarioList.iterator()
    }

    fun possibleTurns(actionsAvailable: ActionsAvailable, targetProximity: Int): List<Turn> {

        val actionList = actionsAvailable.getList(targetProximity)
        println("# possibleTurns, actionsAvailable = $actionsAvailable")
        println("# possibleTurns, actionList(prox) = $actionList")

        val bonusActionNames = character.getPreparedBonusActionSpells(targetProximity).map { it.name }
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
            //var foundAtLeastOneLightWeaponPair = false

            // note: actionList is already filtered by range, which is good
            val lightWeapons = actionList.filter { it is Weapon && it.hasWeaponProperty(WeaponProperty.Light) }
            println("# all light weapons = ${ lightWeapons }")

            // light weapon ?  see if you have a 2nd one to use in a BA
            if (action.hasWeaponProperty(WeaponProperty.Light) && lightWeapons.size > 1)
            {
                println("# light weapon 1=$action, hashcode =${action.hashCode()}, all light weapons = ${ lightWeapons }")

                for (w2 in lightWeapons) {
                    if (action.hashCode() == w2.hashCode()) {
                        continue // the same physical weapon cannot be used for action and BA
                    }
                    // println("# light weapon comparison 1=$action:${action.hashCode()}, 2=$w2:${w2.hashCode()}")

                    turn = Turn(
                        attacks = listOf(
                            // TODO: use weapon nicnkames ?
                            Attack(monster = monster, action = action),
                            Attack(monster = monster, action = w2, isBonusAction = true),
                        )
                    )
                    turnOptions.add(turn)
                }
            }

            // if you didn't find a 2nd light weapon, just use the first weapon w/out a BA
            if (turn == null) {
                turn = Turn(attacks = listOf(Attack(monster = monster, action = action)))
                turnOptions.add(turn)
            }

            // bonus action spells: mostly for ranger and paladin
            for (bonusName in bonusActionNames) {
                try {
                    val bonus = Globals.getSpell(bonusName, character.is2014())
                    turnOptions.add(
                        Turn(
                            attacks = listOf(
                                //Attack(monster = monster, attack = (w1.nickname ?: w1.name)),
                                Attack(monster = monster, action = action),
                                Attack(monster = monster, action = bonus, isBonusAction = true),
                            )
                        )
                    )
                }
                catch (e: Exception) {
                    println("unable to add bonus action $bonusName: $e")
                }
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

        if (spell.properties.Concentration == "Yes") {
            // check spells across all turns in current scenario to determine if any of them might still be running
            for (priorSpell in currentScenario.getSpellsAcrossTurns()) {
                if ((priorSpell.getDuration() ?: 0) > currentScenario.turns.size) {
                    Globals.debug("prior spell still running, new spell requires concentration, so skip it; prior = $priorSpell; skip = $spell")
                    return false
                }
            }
        }

        if (!currentScenario.isSlotAvailable(spell)) {
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
                Globals.debug("lastSpell = $lastSpell, proposed spell = " + a.action.name)
            }

            if (! isAttackValidForScenario(a, currentScenario)) return null
        }

        // make a deep copy of proposed turn, as turn can be modified later (when we add action mods)
        val copy = Turn (proposedTurn.attacks.map {
            a -> Attack(a.monster, a.action, ArrayList(), null, a.isBonusAction)
        }.toMutableList())

        return Scenario (character, currentScenario.turns.map { it.copy() } + copy, currentScenario.numTargets, currentScenario.targetSpacing)
    }

    fun buildScenarios(rounds: Int, turnOptions: List<Turn>, currentScenario: Scenario, scenarioList: ArrayList<Scenario>) {
        if (rounds == 0) {
            scenarioList.add(currentScenario)
            return
        }

        for (turn in turnOptions) {
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
        //Globals.debug("modifiers available=$actionModifiersAvailable")

        for (scenario in scenarioList) {
            var turnId = 0
            for (turn in scenario.turns) {
                for (attack in turn.attacks) {
                    for (mod in actionModifiersAvailable) {
                        val isValid = isActionModifierValidForTurn (mod, scenario, turnId, turn, attack)
                        // Globals.debug("turnId=$turnId, mod=$mod, isValid=$isValid")
                        if (isValid) {
                            attack.actionModifiers.add(mod)
                        }
                    }
                }
                turnId++
            }
        }
    }

    fun testActionsAvailable() {
        val actionsAvailable = character.getActionsAvailable()

        println()
        println("available map: "+actionsAvailable.mapOfLists)
        println()
        println("MELEE:  actions       = "+actionsAvailable.getList(Constants.MELEE_RANGE))
        println("MELEE:  bonus actions = "+character.getPreparedBonusActionSpells(Constants.MELEE_RANGE).map { it.name })
        println()
        println("RANGE:  actions       = "+actionsAvailable.getList(Constants.MELEE_RANGE*2))
        println("RANGE:  bonus actions = "+character.getPreparedBonusActionSpells(Constants.MELEE_RANGE*2).map { it.name })
        println()
    }

    fun hasNext(): Boolean {
        return getPercentComplete() < 100.0f
    }

    fun addNext() {
        resultList.add(ScenarioCalculator(runIterator.next()).calculateDPRForAllTurns())
    }

    fun getPercentComplete(): Float {
        return if (!runIterator.hasNext()) { 100.0f } else { (resultList.size * 1.0f) / (scenarioList.size * 1.0f) }
    }

    fun topResults(max: Int): List<ScenarioResult> {
        // first prioritize totalDPR, and if multiple scenarios have the same total, sort them by highest first round damage
        logger.info { "resultList.size = ${resultList.size}" }
        if (resultList.size == 0) return resultList

        // NOTE: THERE IS NO ROUND ZERO; START AT ONE
        return resultList.sortedWith(
            compareByDescending<ScenarioResult> { it.totalDPR } .thenByDescending { it.dprAtRound(1) }
        ).take(kotlin.math.min(max,resultList.size))
    }

    fun logDuration(label: String, task: () -> Unit) {
        val dur = measureTime { task.invoke() }
        println("# dur($label) = ${dur.inWholeMilliseconds}")
    }

    fun getResultSummary(max: Int): String { // previously, stderr ...
        val buf = StringBuilder()
        for (scenarioResult in topResults(max)) {
            buf.append("# ")
                .append(Globals.getPercent(scenarioResult.totalDPR))
                .append(" \t")
                .append(scenarioResult.scenario.getLabel())
                .append("\n")
        }
        return buf.toString()
    }

    fun showResults() {
        println(getResultSummary(Constants.SCENARIO_OUTPUT_MAX))

        for (scenarioResult in resultList) {
            println(scenarioResult.output())
        }
    }

}
