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
import com.vikinghelmet.dnd.dpr.util.Globals
import dev.shivathapaa.logger.api.LoggerFactory
import kotlinx.serialization.Transient
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.fetchAndIncrement

class Combat()
{
    @Transient private val logger = LoggerFactory.get(Combat::class.simpleName ?: "")
    val teamA = mutableListOf<CombatantWithStatus>()
    val teamB = mutableListOf<CombatantWithStatus>()
    var initiativeList = listOf<CombatantWithStatus>()
    val combatActionList = mutableListOf<CombatAction>()
    var turn = 0

    @OptIn(ExperimentalAtomicApi::class)
    constructor(noStatusTeamA: List<Combatant>, noStatusTeamB: List<Combatant>) : this()
    {
        fun getCounter(team: List<Combatant>): AtomicInt? {
            return if (team.size > 1 && team.all { it == team.firstOrNull() }) AtomicInt(0) else null
        }
        val aCounter = getCounter(noStatusTeamA)
        val bCounter = getCounter(noStatusTeamB)

        fun getNewName(combatant: Combatant, teamCounter: AtomicInt?): String {
            val tmp = combatant.getName().replace(" .*".toRegex(), "")
            return if (teamCounter == null) tmp else "$tmp ${ 'A'+teamCounter.fetchAndIncrement() }"
        }

        noStatusTeamA.forEach { teamA.add(CombatantWithStatus(it, getNewName(it, aCounter), true)) }
        noStatusTeamB.forEach { teamB.add(CombatantWithStatus(it, getNewName(it, bCounter), false)) }
        // when computing initiative order, a DM will often use a single roll for an entire group of monsters
        // we won't do that here - unique rolls are more realistic, and easier to implement
        initiativeList = (teamA + teamB).sortedByDescending { it.currentInitiative }.toList()
    }

    fun run(): Boolean {
        val map = initiativeList.associateBy { it.currentInitiative }
        logger.info { "initiativeList = $map" }
        //logger.info { "initiativeList = $initiativeList" }

        while (!teamA.all { it.isDead() } && !teamB.all { it.isDead() }) {
//            teamA.forEach { logger.info { "turn start, teamA member: ${it.summary()}" } }
//            teamB.forEach { logger.info { "turn start, teamB member: ${it.summary()}" } }

            logger.info { "turn=$turn, teamA: ${ teamA.map { it.summary() }.toList() }, teamB: ${ teamB.map { it.summary() }.toList() }" }
            fullTurn()
            turn++
        }
        if (!teamA.all { it.isDead() }) {
            logger.info { "winner = teamA = $teamA " }
            return true
        }
        else {
            logger.info { "winner = teamB = $teamB " }
            return false
        }
    }

    fun fullTurn() {
        initiativeList.forEach { combatant ->
            if (combatant.isDead()) {
                logger.debug { "fullTurn, turn=$turn, combatant=$combatant, is dead"}
            }
            else if (combatant.isDying()) {
                // TODO: roll for death saving throw
                combatant.deathSave()
                logger.debug { "fullTurn, turn=$turn, combatant=$combatant, after death saving throw, save list: ${combatant.deathSavingThrows}, currentHP: ${combatant.currentHP}" }
            }
            else if (!combatant.canTakeAction()) {
                logger.info { "fullTurn, turn=$turn, combatant=$combatant, can not take action"}
            }
            else {
                logger.info { "fullTurn, turn=$turn, combatant=$combatant is taking action"}
                takeTurn(combatant)
            }
        }
    }

    fun takeTurn(combatant: CombatantWithStatus) {
        val target = chooseTarget(combatant)
        if (target == null) {
            logger.debug { "turn = $turn, combatant = ${combatant.shortName()}, no target available" }
            return
        }
        val attackList = chooseTurnActions(combatant, target)

        logger.debug { "turn = $turn, combatant = ${combatant.shortName()}, selected target = ${target.shortName()}" }
        for (attack in attackList) {
            if (attack.action is Weapon) {
                attackWithWeapon(combatant, target, attack)
            }
            else {
                // getSpellDPR(turnId, actionId, attack, actionCalculator)
            }
        }
    }

    fun chooseTarget(combatant: CombatantWithStatus): CombatantWithStatus? {
        var targetList = (if (combatant.onTeamA) teamB else teamA).filter { !it.isDeadOrDying() }.toList()
        if (targetList.isEmpty()) {
            // if all you have are a few dying opponents, keep sticking a fork in them until they're done
            targetList = (if (combatant.onTeamA) teamB else teamA).filter { !it.isDead() }.toList()
        }
        if (targetList.isEmpty()) {
            return null
        }

        // if you already have a target that is not dead, keep at it
        if (combatant.target != null && !combatant.target!!.isDeadOrDying()) {
            logger.verbose { "chooseTarget: keeping current target = ${combatant.target}" }
            return combatant.target!!
        }

        // if you are currently someone else's target, target them back
        targetList.forEach { if (it.target == combatant) {
            logger.debug { "choosing a new target that is already attacking: ${combatant.target!!}" }
            return it
        } }

        // if you are within melee range, you can't move (don't provoke an oppy attack) ...
        // might as well attack
        val inMeleeRange = targetList.any { combatant.distance(it) <= 1 }
        if (inMeleeRange) {
            val result = targetList.filter { combatant.distance(it) <= 1 }.random() // TODO: improve target selection
            logger.debug { "choosing a new target that is inMeleeRange: $result" }
            return result
        }

        // now that we know we aren't in melee range, it is safe to move about the playing field as needed

        val closest = targetList.minByOrNull { combatant.distance(it) }
        var closestDistance = closest!!.distance(combatant)
        val preferredDistance = combatant.getPreferredCombatDistance()
        var initialLoc = combatant.location.copy()

        fun logMovement(toOrFrom: String) {
            val buf = StringBuilder(combatant.shortName()).append(": ")
                .append(toOrFrom)
                .append(", initialLoc = $initialLoc")
                .append(", newLoc = ${combatant.location}")
                .append(", preferredDistance = $preferredDistance")
                .append(", closestDistance = ${Globals.getPercent(closestDistance.toFloat())}")
            logger.debug { buf.toString() }
        }

        if (preferredDistance <= Constants.MELEE_RANGE) {
            // pick a target, then move towards it
            combatant.moveTowardTarget(closest)    // TODO: improve target selection
            closestDistance = closest.distance(combatant)
            logMovement("moving toward melee target $closest")
            return closest
        }

        // if you are too close for comfort ... run away before picking a target
        if (closestDistance <= preferredDistance) {
            closestDistance = combatant.moveAwayFromTarget (targetList, closestDistance)
            logMovement("moving away from targets")
        }

        val result = targetList.minByOrNull { it.distance(combatant.location) }!! // TODO: improve target selection
        logger.debug { "choosing a new target: $result" }
        return result
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
        if (scenarioResultList.isEmpty()) return emptyList()

        val bestResult = ScenarioResult.topResults(scenarioResultList, 1)[0]
        return bestResult.scenario.turns[0].attacks
    }

    fun attackWithWeapon(combatant: CombatantWithStatus, target: CombatantWithStatus, attack: Attack)
    {
        if (attack.action !is Weapon) return
        var attackRoll = (1..20).random()
        if (target.attackersHaveAdvantage && !combatant.disadvantageOnAttacks) {
            logger.info { "attacker has advantage" }
            attackRoll = kotlin.math.max(attackRoll, (1..20).random())
        }
        else if (!target.attackersHaveAdvantage && combatant.disadvantageOnAttacks) {
            logger.info { "attacker has disadvantage" }
            attackRoll = kotlin.math.min(attackRoll, (1..20).random())
        }

        logger.debug { "combatant = $combatant, target = $target, weapon = ${attack.action.name}" }

        // TODO: bless and bane, maybe others?
        //var bonusDiceToHit: DiceBlock = DiceBlock(0, 0, 0, 0, 0)
        //var penaltyDiceToHit: DiceBlock = DiceBlock(0, 0, 0, 0, 0)

        attackRoll += attack.action.getAttackBonus()

        val autoHit = attackRoll == 20 // critical Hit + Damage ... TODO: for a champion, autoHit on 19 or 18

        if (attackRoll >= target.getAC() || autoHit) {
            val isCrit = autoHit || target.attackerAutoCrit
            val damage = computeDamage(attack, target, isCrit)
            target.currentHP -= damage
            logger.debug { "combatant = $combatant, target = $target, weapon = ${attack.action.name}, attackRoll = ${attackRoll}, hit = true, damage = $damage, remainingHP = ${ target.currentHP}" }
        }
        else {
            logger.debug { "combatant = $combatant, target = $target, weapon = ${attack.action.name}, attackRoll = ${attackRoll}, hit = false" }
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

        logger.debug { "target = $target, damage = $totalDamage" }
        return totalDamage
    }
}
