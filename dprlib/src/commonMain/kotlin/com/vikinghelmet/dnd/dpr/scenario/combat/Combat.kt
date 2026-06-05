package com.vikinghelmet.dnd.dpr.scenario.combat

import com.vikinghelmet.dnd.dpr.action.Attack
import com.vikinghelmet.dnd.dpr.action.Combatant
import com.vikinghelmet.dnd.dpr.action.Damage
import com.vikinghelmet.dnd.dpr.action.Weapon
import com.vikinghelmet.dnd.dpr.action.enums.DamageType
import com.vikinghelmet.dnd.dpr.scenario.onesided.ScenarioBuilder
import com.vikinghelmet.dnd.dpr.scenario.onesided.ScenarioCalculator
import com.vikinghelmet.dnd.dpr.scenario.onesided.ScenarioResult
import com.vikinghelmet.dnd.dpr.util.Constants
import com.vikinghelmet.dnd.dpr.util.Constants.DEFAULT_NUM_TARGETS
import com.vikinghelmet.dnd.dpr.util.Constants.DEFAULT_TARGET_SPACING
import dev.shivathapaa.logger.api.LoggerFactory
import kotlinx.serialization.Transient

class Combat()
{
    @Transient private val logger = LoggerFactory.get(Combat::class.simpleName ?: "")
    val teamA = mutableListOf<CombatantWithStatus>()
    val teamB = mutableListOf<CombatantWithStatus>()
    var initiativeList = listOf<CombatantWithStatus>()
    val combatActionList = mutableListOf<CombatAction>()
    var turn = 0

    constructor(noStatusTeamA: List<Combatant>, noStatusTeamB: List<Combatant>) : this()
    {
        noStatusTeamA.forEach { teamA.add(CombatantWithStatus(it, true)) }
        noStatusTeamB.forEach { teamB.add(CombatantWithStatus(it, false)) }
        // when computing initiative order, a DM will often use a single roll for an entire group of monsters
        // we won't do that here - unique rolls are more realistic, and easier to implement
        initiativeList = (teamA + teamB).sortedByDescending { it.currentInitiative }.toList()
    }

    fun run() {
        val map = initiativeList.associateBy { it.currentInitiative }
        logger.info { "initiativeList = $map" }
        //logger.info { "initiativeList = $initiativeList" }
        while (!teamA.all { it.isDead() } && !teamB.all { it.isDead() }) {
            teamA.forEach { logger.info { "turn start, teamA member: ${it.getName()}, isDead=${it.isDead()}, isDying=${it.isDying()}" } }
            teamB.forEach { logger.info { "turn start, teamB member: ${it.getName()}, isDead=${it.isDead()}, isDying=${it.isDying()}" } }
            fullTurn()
            turn++
        }
        if (!teamA.all { it.isDead() }) {
            logger.info { "winner = teamA = $teamA " }
        }
        else {
            logger.info { "winner = teamB = $teamB " }
        }
    }

    fun fullTurn() {
        initiativeList.forEach { combatant ->
            if (combatant.isDead()) {
                logger.info { "combatant is dead: $combatant"}
            }
            else if (combatant.isDying()) {
                // TODO: roll for death saving throw
                combatant.deathSave()
                logger.info { "after death saving throw, save list: ${combatant.deathSavingThrows}, currentHP: ${combatant.currentHP}" }
            }
            else if (!combatant.canTakeAction()) {
                logger.info { "combatant can not take action: $combatant"}
            }
            else {
                takeTurn(combatant)
            }
        }
    }

    fun takeTurn(combatant: CombatantWithStatus) {
        val target = chooseTarget(combatant)
        val attackList = chooseTurnActions(combatant, target)

        logger.info { "turn = $turn, combatant = ${combatant.getName()}, selected target = ${target.getName()}" }
        for (attack in attackList) {
            if (attack.action is Weapon) {
                attackWithWeapon(combatant, target, attack)
            }
            else {
                // getSpellDPR(turnId, actionId, attack, actionCalculator)
            }
        }
    }

    fun chooseTarget(combatant: CombatantWithStatus): CombatantWithStatus {
        val targetList = if (combatant.onTeamA) teamB else teamA

        // if you already have a target that is not dead, keep at it
        if (combatant.target != null) return combatant.target!!

        // if you are currently someone else's target, target them back
        targetList.forEach { if (it.target == combatant) return it }

        // if you are within melee range, you can't move (don't provoke an oppy attack) ...
        // might as well attack
        val inMeleeRange = targetList.any { combatant.distance(it) <= 1 }
        if (inMeleeRange) {
            return targetList.filter { combatant.distance(it) <= 1 }.random() // TODO: improve target selection
        }

        // now that we know we aren't in melee range, it is safe to move about the playing field as needed

        val closest = targetList.minByOrNull { combatant.distance(it) }
        val closestDistance = closest!!.distance(combatant)
        val preferredDistance = combatant.getPreferredCombatDistance()

        if (preferredDistance <= Constants.MELEE_RANGE) {
            // pick a target, then move towards it
            combatant.moveTowardTarget(closest)    // TODO: improve target selection
            return closest
        }

        // if you are too close for comfort ... run away before picking a target
        if (closestDistance <= preferredDistance) {
            combatant.moveAwayFromTarget (targetList, closestDistance)
        }

        return targetList.minByOrNull { it.distance(combatant.location) }!! // TODO: improve target selection
    }

    fun chooseTurnActions(combatant: CombatantWithStatus, target: CombatantWithStatus): List<Attack> {
        val builder = ScenarioBuilder(combatant, target)
        val distance = combatant.distance(target)
        // TODO: compute numTargets and spacing from targetList
        val scenarioList = builder.build(distance.toInt(), 1, DEFAULT_NUM_TARGETS, DEFAULT_TARGET_SPACING)

        // choose an attack based on highest damage probability
        // TODO: characters with higher INT should be able to "lookahead" more turns (better planning ability)
        // TODO: account for spell slots and other limited-resource actions
        val scenarioResultList = scenarioList.map { ScenarioCalculator(it).calculateDPRForAllTurns() }.toList()
        val bestResult = ScenarioResult.topResults(scenarioResultList, 1)[0]
        return bestResult.scenario.turns[0].attacks
    }

    fun attackWithWeapon(combatant: CombatantWithStatus, target: CombatantWithStatus, attack: Attack)
    {
        if (attack.action !is Weapon) return
        var attackRoll = (1..20).random()
        if (target.attackersHaveAdvantage && !combatant.disadvantageOnAttacks) {
            attackRoll = kotlin.math.max(attackRoll, (1..20).random())
        }
        else if (!target.attackersHaveAdvantage && combatant.disadvantageOnAttacks) {
            attackRoll = kotlin.math.min(attackRoll, (1..20).random())
        }

        logger.info { "combatant = $combatant, target = $target, weapon = ${attack.action.name}" }

        // TODO: bless and bane, maybe others?
        //var bonusDiceToHit: DiceBlock = DiceBlock(0, 0, 0, 0, 0)
        //var penaltyDiceToHit: DiceBlock = DiceBlock(0, 0, 0, 0, 0)

        attackRoll += attack.action.getAttackBonus()

        val autoHit = attackRoll == 20 // critical Hit + Damage ... TODO: for a champion, autoHit on 19 or 18

        if (attackRoll >= target.getAC() || autoHit) {
            val isCrit = autoHit || target.attackerAutoCrit
            val damage = computeDamage(attack, target, isCrit)
            target.currentHP -= damage
            logger.info { "combatant = $combatant, target = $target, weapon = ${attack.action.name}, attackRoll = ${attackRoll}, hit = true, damage = $damage, remainingHP = ${ target.currentHP}" }
        }
        else {
            logger.info { "combatant = $combatant, target = $target, weapon = ${attack.action.name}, attackRoll = ${attackRoll}, hit = false" }
        }
    }

    fun computeDamage(attack: Attack, target: CombatantWithStatus, isCrit: Boolean): Int
    {
        if (attack.action !is Weapon) return 0
        val damageList = attack.action.getDamageList().toMutableList()

        for (modifier in attack.actionModifiers) {
            val bonusDamage = modifier.getBonusDamage()
            if (!bonusDamage.isEmpty()) {
                damageList.add(Damage(bonusDamage.copy(), 0, 0, DamageType.undefined))
            }
        }

        var totalDamage = 0
        for (it in damageList) {
            if (target.getDamageImmunities().contains(it.type)) {
                logger.info { "target has immunity ${it.type}" }
                continue
            }
            var effectDamage = it.dice.roll() + it.bonus
            if (attack.isBonusAction != true) {
                effectDamage += it.abilityBonus
            }
            if (target.getDamageResistances().contains(it.type)) {
                effectDamage /= 2
            }
            if (target.getDamageVulnerabilities().contains(it.type)) {
                effectDamage *= 2
            }
            totalDamage += effectDamage
        }

        logger.info { "target = $target, damage = $totalDamage" }
        return totalDamage
    }
}
