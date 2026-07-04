package com.vikinghelmet.dnd.dpr.scenario.onesided

import com.vikinghelmet.dnd.dpr.action.Attack
import com.vikinghelmet.dnd.dpr.action.Combatant
import com.vikinghelmet.dnd.dpr.action.Turn
import com.vikinghelmet.dnd.dpr.action.Weapon
import com.vikinghelmet.dnd.dpr.character.PlayerCharacter
import com.vikinghelmet.dnd.dpr.character.actions.ActionModifier
import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import com.vikinghelmet.dnd.dpr.scenario.ActionsAvailable
import com.vikinghelmet.dnd.dpr.scenario.TurnBuilder
import com.vikinghelmet.dnd.dpr.spells.Spell
import com.vikinghelmet.dnd.dpr.util.Globals
import dev.shivathapaa.logger.api.LoggerFactory
import kotlin.time.measureTime

class ScenarioBuilder(
    val attacker: Combatant,
    val target: Combatant,
    val actionsAvailable: ActionsAvailable
) {
    val logger = LoggerFactory.get(ScenarioBuilder::class.simpleName ?: "no simpleName")

    var turnOptions: MutableList<Turn> = mutableListOf() // these intermediate results may be displayed to user

    constructor(attacker: Combatant, target: Combatant) : this(attacker, target, attacker.getActionsAvailable()) {

    }
    fun build(targetProximity: Int, numberOfTurns: Int, numTargets: Int, targetSpacing: Int): List<Scenario>
    {
        var scenarioList = ArrayList<Scenario>()

        logDuration("possibleTurns", {
            turnOptions.addAll (TurnBuilder(attacker, target).getPossibleTurns (actionsAvailable, targetProximity))
        })
        logger.debug { "# num(possibleTurns) = ${turnOptions.size}" }

        logDuration("buildScenarios", {
            buildScenarios(
                numberOfTurns,
                turnOptions,
                Scenario(attacker, emptyList(), numTargets, targetSpacing),
                scenarioList
            )
        })

        logger.debug {"# num(scenarios) = ${scenarioList.size}" }

        logDuration("addActionModifiers", { addActionModifiers(scenarioList) })
        return scenarioList
    }

    fun isAttackValidForScenario(proposedAttack: Attack, currentScenario: Scenario): Boolean
    {
        if (proposedAttack.action !is Spell) return true
        val spell = proposedAttack.action

        if (currentScenario.getSpellsAcrossTurns().isEmpty()) { // no spells used, no conflict, just add it
            return true
        }

        if (spell.requiresConcentration()) {
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
            a -> Attack(a.target, a.action, ArrayList(), a.isBonusAction)
        }.toMutableList())

        return Scenario (attacker, currentScenario.turns.map { it.copy() } + copy, currentScenario.numTargets, currentScenario.targetSpacing)
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
        if (attacker !is PlayerCharacter) {
            Globals.debug("for now, all modifiers are PC-only")
            return false
        }

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
                // limited use per day (tied to WIS bonus)
                return countModifier(mod, scenario) < attacker.getAbilityModifier(AbilityType.Wisdom)
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
        val actionModifiersAvailable = attacker.getActionModifiersAvailable()
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

    fun logDuration(label: String, task: () -> Unit) {
        val dur = measureTime { task.invoke() }
        logger.debug { "# dur($label) = ${dur.inWholeMilliseconds}" }
    }
}
