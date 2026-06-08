package com.vikinghelmet.dnd.dpr.scenario.combat

import com.vikinghelmet.dnd.dpr.action.*
import com.vikinghelmet.dnd.dpr.action.enums.DamageType
import com.vikinghelmet.dnd.dpr.monsters.Monster
import com.vikinghelmet.dnd.dpr.spells.SaveResult.*
import com.vikinghelmet.dnd.dpr.spells.SavingThrowAction
import com.vikinghelmet.dnd.dpr.spells.Spell
import com.vikinghelmet.dnd.dpr.spells.SpellAttack
import dev.shivathapaa.logger.api.LoggerFactory
import kotlinx.serialization.Transient
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.fetchAndIncrement
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalAtomicApi::class)
class Combat(val battleId: Int) {
    @Transient
    private val logger = LoggerFactory.get(Combat::class.simpleName ?: "")
    val teamA = mutableListOf<CombatantWithStatus>()
    val teamB = mutableListOf<CombatantWithStatus>()
    var initiativeList = listOf<CombatantWithStatus>()
    val attackResultList = mutableListOf<CombatAttackResult>()
    var turnId = 0
    var actionId = 0
    var effectId = 0

    constructor(battleId: Int, noStatusTeamA: List<Combatant>, noStatusTeamB: List<Combatant>, flightSupported: Boolean = false)
        : this(battleId)
    {
        val aCounter = getCounter(noStatusTeamA)
        val bCounter = getCounter(noStatusTeamB)

        noStatusTeamA.forEach {
            teamA.add (CombatantWithStatus (it, getNewName(it, aCounter), true, flightSupported = flightSupported))
        }

        noStatusTeamB.forEach {
            teamB.add (CombatantWithStatus (it, getNewName(it, bCounter), false, flightSupported = flightSupported))
        }

        // when computing initiative order, a DM will often use a single roll for an entire group of monsters
        // we won't do that here - unique rolls are more realistic, and easier to implement
        initiativeList = (teamA + teamB).sortedByDescending { it.initiative }.toList()
    }

    private fun getCounter(team: List<Combatant>): AtomicInt? {
        return if (team.size > 1 && team.all { it == team.firstOrNull() }) AtomicInt(0) else null
    }

    private fun getNewName(combatant: Combatant, teamCounter: AtomicInt?): String {
        val tmp = if (combatant is Monster) {
            combatant.getName().replace(" ".toRegex(), "") // just eliminate whitespace
        }
        else {
            combatant.getName().replace(" .*".toRegex(), "") // PC: eliminate everything after+including first whitespace
        }
        // for a team of equals, append a letter suffix
        return if (teamCounter == null) tmp else "$tmp ${'A' + teamCounter.fetchAndIncrement()}"
    }

    fun run(): Boolean {
        logger.info { "initiativeList = ${ initiativeList.associateBy { it.initiative }}" }

        while (!teamA.all { it.isDead() } && !teamB.all { it.isDead() }) {
            logger.info {
                "battleId=$battleId, turn=$turnId, teamA: ${ teamSummary(teamA) }, teamB: ${ teamSummary(teamB) }"
            }
            fullTurn()
            turnId++
            actionId = 0
            effectId = 0
        }
        if (!teamA.all { it.isDead() }) {
            logger.info { "battleId=$battleId, turn=$turnId, winner = teamA = ${ teamSummary(teamA) } " }
            return true
        } else {
            logger.info { "battleId=$battleId, turn=$turnId, winner = teamB = ${ teamSummary(teamB) } " }
            return false
        }
    }

    fun teamSummary(team: List<CombatantWithStatus>): String {
        return "${ team.sortedByDescending { it.initiative }.map { it.summary() }.toList() }"
    }

    fun fullTurn() {
        initiativeList.forEach { combatant ->
            if (combatant.isDead()) {
                logger.debug { "fullTurn, turn=$turnId, combatant=$combatant, is dead" }
            } else if (combatant.isDying()) {
                // TODO: roll for death saving throw
                combatant.deathSave()
                logger.info { "fullTurn, turn=$turnId, combatant=$combatant, after death saving throw, save list: ${combatant.deathSavingThrows}, currentHP: ${combatant.currentHP}" }
            } else if (!combatant.canTakeAction()) {
                logger.info { "fullTurn, turn=$turnId, combatant=$combatant, can not take action" }
            } else {
                logger.debug { "fullTurn, turn=$turnId, combatant=$combatant is taking action" }
                takeTurn(combatant)
            }
        }
    }

    fun takeTurn(combatant: CombatantWithStatus) {
        val target = chooseTarget(combatant)
        combatant.target = target
        if (target == null) {
            logger.debug { "turn = $turnId, combatant = ${combatant.shortName()}, no target available" }
            return
        }
        val attackList = chooseTurnActions(combatant, target)
        val attackResultsThisTurn = mutableListOf<CombatAttackResult>()

        logger.debug { "turn = $turnId, combatant = ${combatant.shortName()}, selected target = ${target.shortName()}" }

        for (attack in attackList) {
            if (attack.action is Weapon) {
                attackResultsThisTurn.addAll (meleeOrRangeAttack(combatant, target, attack, attack.action))
            } else {
                attackResultsThisTurn.addAll (attackWithSpell(combatant, target, attack))
            }
            actionId++
        }
        logger.info { "turn = $turnId, attackResults = $attackResultsThisTurn" }

        attackResultList.addAll(attackResultsThisTurn)
    }

    fun chooseTarget(combatant: CombatantWithStatus): CombatantWithStatus?
    {
        var targetList: List<CombatantWithStatus>

        // if you already have a target that is not dead/dying, try to finish them off
        if (combatant.target != null && !combatant.target!!.isDeadOrDying()) {
            logger.verbose { "chooseTarget: keeping current target = ${combatant.target}" }
            targetList = listOf(combatant.target!!)
        }
        else {
            targetList = (if (combatant.onTeamA) teamB else teamA).filter { !it.isDeadOrDying() }.toList()
        }

        if (targetList.isEmpty()) {
            // if all you have are a few dying opponents, keep sticking a fork in them until they're done
            targetList = (if (combatant.onTeamA) teamB else teamA).filter { !it.isDead() }.toList()
        }
        if (targetList.isEmpty()) {
            return null // early return because we have no targets
        }

        // if you are within melee range, you can't move (don't provoke an oppy attack) ...
        // just attack someone right in front of you
        val inMeleeRange = targetList.filter { !it.isDead() && combatant.distance(it) <= Distance.melee() }
        if (inMeleeRange.isNotEmpty()) {
            return TargetSelector(this, combatant, inMeleeRange).select().first  // early return because we can't move
        }

        // if you are currently someone else's target, target them back
        val attackingMeList = targetList.filter { it.target == combatant }.toList()
        if (!attackingMeList.isEmpty()) {
            targetList = attackingMeList
        }

        val target = TargetSelector(this, combatant, targetList).select().first ?: return null

        // now that we know we aren't in melee range, it is safe to move about the playing field as needed

        if (combatant.getPreferredCombatDistance() <= Distance.melee()) {
            combatant.moveTowardTarget(target)
        }
        else {
            var distance = target.distance(combatant)
            if (distance <= combatant.getPreferredCombatDistance()) { // too close for comfort
                combatant.moveAwayFromTarget(targetList, distance)
            }
        }

        return target
    }

    fun chooseTurnActions(combatant: CombatantWithStatus, target: CombatantWithStatus): List<Attack> {
        if (combatant.combatant is Monster) {
            logger.info { "chooseTurnActions: waitingForRecharge: ${combatant.combatant.waitingForRecharge}" }
        }
        val distance = combatant.distance(target)
        val preferredTurnOption = combatant.getPreferredTurn(target, distance.toFeet())

        if (preferredTurnOption != null) {
            val spell = preferredTurnOption.getSpell()
            if (spell != null) {
                combatant.spellCastList.add(SpellCast(combatant, spell, turnId))
            }
        }

        return preferredTurnOption?.attacks ?: emptyList()
    }

    fun getAttackRoll(combatant: CombatantWithStatus, target: CombatantWithStatus): Int {
        var attackRoll = (1..20).random()
        if (target.attackersHaveAdvantage && !combatant.disadvantageOnAttacks) {
            logger.info { "attacker has advantage" }
            attackRoll = max(attackRoll, (1..20).random())
        } else if (!target.attackersHaveAdvantage && combatant.disadvantageOnAttacks) {
            logger.info { "attacker has disadvantage" }
            attackRoll = min(attackRoll, (1..20).random())
        }
        return attackRoll
    }

    fun meleeOrRangeAttack(
        combatant: CombatantWithStatus,
        target: CombatantWithStatus,
        attack: Attack,
        action: MeleeOrRangeAction
    ) : List<CombatAttackResult>
    {
        var attackRoll = getAttackRoll(combatant, target)
        val name = action.getActionName()
        logger.debug { "combatant = $combatant, target = $target, action = $name" }

        // TODO: bless and bane, maybe others?
        //var bonusDiceToHit: DiceBlock = DiceBlock(0, 0, 0, 0, 0)
        //var penaltyDiceToHit: DiceBlock = DiceBlock(0, 0, 0, 0, 0)

        attackRoll += action.getAttackBonus()

        val autoHit = attackRoll == 20 // critical Hit + Damage ... TODO: for a champion, autoHit on 19 or 18
        var damage = 0

        if (attackRoll >= target.getAC() || autoHit) {
            val isCrit = autoHit || target.attackerAutoCrit
            damage = computeDamage(attack, target, isCrit, action.getDamageList())
            target.currentHP -= damage
        }

        return listOf (CombatAttackResult (combatant, listOf(target), damage, attack, turnId, actionId, effectId++))
    }

    fun computeDamage(attack: Attack, target: CombatantWithStatus, isCrit: Boolean, baseDamageList: List<Damage>): Int {
        val damageList = baseDamageList.toMutableList()

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
            val roll = it.dice.roll()
            var effectDamage = (if (isCrit) roll * 2 else roll) + it.bonus
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

    fun attackWithSpell(combatant: CombatantWithStatus, target: CombatantWithStatus, attack: Attack)
        : List<CombatAttackResult>
    {
        val attackBonus = combatant.getSpellBonusToHit()
        val spell = attack.action as Spell
        logger.debug { "spell = ${spell.fullString()}" }

        val result = mutableListOf<CombatAttackResult>()
        for (spellAttack in spell.getSpellAttacks(attackBonus)) {
            logger.debug { "spell = ${spell.name}, spellAttack = $spellAttack" }

            if (spellAttack.isNoDamageAttack()) {
                logger.debug { "no damage" }
                continue
            }

            if (spellAttack.isSavingThrowAttack()) {
                result.addAll (castSavingThrowSpell (combatant, target, spellAttack, attack))
            } else {
                result.addAll (meleeOrRangeAttack (combatant, target, attack, spellAttack))
            }
        }

        return result
    }

    fun castSavingThrowSpell(
        combatant: CombatantWithStatus,
        target: CombatantWithStatus,
        spellAttack: SpellAttack,
        attack: Attack
    ) : List<CombatAttackResult>
    {
        if (spellAttack.isNoDamageAttack()) {
            logger.debug { "This spell never directly creates damage" }
            return emptyList()
        }

        val save = spellAttack.attackPayload.save!!

        // TODO: area of effect spells (multiple targets)

        // TODO: add support for Hunters Mark damage on melee/range spell attacks

        var successfulSave = false

        if (!combatant.autoFailSave.contains(save.saveAbility)) {
            var saveRoll = (1..20).random()
            if (target.disadvantageOnSave.any { it2 -> it2.match(save.saveAbility) }) {
                saveRoll = max(saveRoll, (1..20).random())
            }

            // TODO: bless/bane -> bonus/penalty dice to save
            val targetSaveBonus = attack.target.getAbilityModifier(save.saveAbility)
            successfulSave = (saveRoll + targetSaveBonus >= combatant.getSpellSaveDC())
        }

        logger.debug { "successfulSave = $successfulSave" }

        var initialDamage = computeDamage(attack, target, false, spellAttack.getDamageList())
        logger.debug { "initial damage = $initialDamage" }

        val finalDamage = applySavingThrowDamageModifiers(spellAttack, attack, initialDamage, successfulSave)
        target.currentHP -= finalDamage

        // TODO: on a failed save add conditions to target

        // if breath weapon or similar, add to the recharge list
        if (combatant.combatant is Monster && attack.action is SavingThrowAction) {
            logger.info { "add attack to waitingForRecharge: ${attack.action}" }
            combatant.combatant.waitingForRecharge.add(attack.action)
        }

        return listOf (CombatAttackResult (combatant, listOf(target), finalDamage, attack, turnId, actionId, effectId++))
    }

    fun applySavingThrowDamageModifiers(
        spellAttack: SpellAttack,
        attack: Attack,
        initialDamage: Int,
        successfulSave: Boolean
    ): Int {
        var damage = initialDamage
        val saveResult = spellAttack.getSaveResult()
        val isEvasive = attack.target.isEvasive()
        logger.debug { "saveResult (onSuccess) = ${saveResult.name}, isEvasive = $isEvasive" }

        if (!successfulSave) {
            if (isEvasive) damage /= 2
        } else {
            when (saveResult) {
                SPELL_ENDS -> {
                    logger.debug { "spell ends" } // TODO: update condition list ?
                    return 0
                }

                CONDITION_ENDS -> {
                    logger.debug { "condition ends" } // TODO: update condition list ?
                    return 0
                }

                NO_EFFECT -> damage = 0
                HALF_DAMAGE -> {
                    if (isEvasive) damage = 0 else damage /= 2
                }

                else -> {}
            }
        }

        logger.debug { "final damage = $damage" }
        return damage
    }
}