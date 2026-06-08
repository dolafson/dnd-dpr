package com.vikinghelmet.dnd.dpr.scenario.combat

import com.vikinghelmet.dnd.dpr.action.*
import com.vikinghelmet.dnd.dpr.action.enums.DamageType
import com.vikinghelmet.dnd.dpr.monsters.Monster
import com.vikinghelmet.dnd.dpr.spells.SaveResult.*
import com.vikinghelmet.dnd.dpr.spells.SavingThrowAction
import com.vikinghelmet.dnd.dpr.spells.SpellAttack
import dev.shivathapaa.logger.api.LoggerFactory
import kotlinx.serialization.Transient
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.fetchAndIncrement

@OptIn(ExperimentalAtomicApi::class)
class Combat() {
    @Transient
    private val logger = LoggerFactory.get(Combat::class.simpleName ?: "")
    val teamA = mutableListOf<CombatantWithStatus>()
    val teamB = mutableListOf<CombatantWithStatus>()
    var initiativeList = listOf<CombatantWithStatus>()
    val attackResultList = mutableListOf<CombatAttackResult>()
    var turnId = 0
    var actionId = 0
    var effectId = 0

    constructor(noStatusTeamA: List<Combatant>, noStatusTeamB: List<Combatant>, flightSupported: Boolean = false)
        : this()
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
                "turn=$turnId, teamA: ${
                    teamA.map { it.summary() }.toList()
                }, teamB: ${teamB.map { it.summary() }.toList()}"
            }
            fullTurn()
            turnId++
            actionId = 0
            effectId = 0
        }
        if (!teamA.all { it.isDead() }) {
            logger.info { "winner = teamA = $teamA " }
            return true
        } else {
            logger.info { "winner = teamB = $teamB " }
            return false
        }
    }

    fun fullTurn() {
        initiativeList.forEach { combatant ->
            if (combatant.isDead()) {
                logger.debug { "fullTurn, turn=$turnId, combatant=$combatant, is dead" }
            } else if (combatant.isDying()) {
                // TODO: roll for death saving throw
                combatant.deathSave()
                logger.debug { "fullTurn, turn=$turnId, combatant=$combatant, after death saving throw, save list: ${combatant.deathSavingThrows}, currentHP: ${combatant.currentHP}" }
            } else if (!combatant.canTakeAction()) {
                logger.info { "fullTurn, turn=$turnId, combatant=$combatant, can not take action" }
            } else {
                logger.info { "fullTurn, turn=$turnId, combatant=$combatant is taking action" }
                takeTurn(combatant)
            }
        }
    }

    fun takeTurn(combatant: CombatantWithStatus) {
        val target = chooseTarget(combatant)
        if (target == null) {
            logger.debug { "turn = $turnId, combatant = ${combatant.shortName()}, no target available" }
            return
        }
        val attackList = chooseTurnActions(combatant, target)

        logger.debug { "turn = $turnId, combatant = ${combatant.shortName()}, selected target = ${target.shortName()}" }
        for (attack in attackList) {
            if (attack.action is Weapon) {
                attackResultList.addAll (meleeOrRangeAttack(combatant, target, attack, attack.action))
            } else {
                attackResultList.addAll (attackWithSpell(combatant, target, attack))
            }
            actionId++
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
        val attackingMeList = targetList.filter { it.target == combatant }.toList()
        if (!attackingMeList.isEmpty()) {
            logger.debug { "choosing a new target that is already attacking me: $attackingMeList" }
            return chooseNewTarget(combatant, attackingMeList).first
        }

        // if you are within melee range, you can't move (don't provoke an oppy attack) ...
        // might as well attack
        val inMeleeRange = targetList.filter { combatant.distance(it) <= Distance.melee() }
        if (inMeleeRange.isNotEmpty()) {
            return chooseNewTarget(combatant, inMeleeRange).first
        }

        val target = chooseNewTarget(combatant, targetList).first ?: return null

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
            attackRoll = kotlin.math.max(attackRoll, (1..20).random())
        } else if (!target.attackersHaveAdvantage && combatant.disadvantageOnAttacks) {
            logger.info { "attacker has disadvantage" }
            attackRoll = kotlin.math.min(attackRoll, (1..20).random())
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
        val spell = attack.action as com.vikinghelmet.dnd.dpr.spells.Spell
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
                saveRoll = kotlin.math.max(saveRoll, (1..20).random())
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

    fun chooseNewTarget(combatant: CombatantWithStatus, targetList: List<CombatantWithStatus>)
        : Pair<CombatantWithStatus?, TargetSelectionStrategy>
    {
        val team = (if (combatant.onTeamA) teamA else teamB)

        // find damagedFriend with minimal HP and distance
        val damagedFriend = team.filter { friend -> friend != combatant && friend.currentHP < friend.getHP()/2 }
            .sortedBy { it.currentHP * combatant.distance(it).toFeet() }
            .firstOrNull()

        if (damagedFriend != null) {
            val targetAttackingFriend = targetList.filter { it.target == damagedFriend }
                .filter { combatant.distance(it) < Distance.fromFeet(30) }  // can we get to them in time ?
                .firstOrNull()
            if (targetAttackingFriend != null) {
                logger.debug { "selecting target attacking damaged friend: $targetAttackingFriend" }
                return Pair(targetAttackingFriend, TargetSelectionStrategy.targetAttackingFriendWhoIsAlmostDead)
            }
        }

        // target that has done a lot of damage to you personally

        var heavyHitter = attackResultList
            .filter { it.targetList.contains(combatant) } // damaged you personally
            .groupBy { it.combatant }
            .mapValues { entry -> entry.value.sumOf { it.totalDamage } }
            .toList()
            .filter { it.second > combatant.getHP() / 4 } // TODO: right percentage ?
            .sortedByDescending { it.second }
            .firstOrNull()

        if (heavyHitter != null) {
            logger.debug { "selecting target with high damage to combatant: ${heavyHitter!!.first}" }
            return Pair(heavyHitter.first, TargetSelectionStrategy.targetWithHighDamageToAttacker)
        }

        // target that has done a lot of damage to the party as a whole

        heavyHitter = attackResultList
            .groupBy { it.combatant }
            .mapValues { entry -> entry.value.sumOf { it.totalDamage } }
            .toList()
            .filter { it.second > combatant.getHP() / 2 }  // TODO: right percentage ?
            .sortedByDescending { it.second }
            .firstOrNull()

        if (heavyHitter != null) {
            logger.debug { "selecting target with high damage to party: ${heavyHitter!!.first}" }
            return Pair(heavyHitter.first, TargetSelectionStrategy.targetWithHighDamageToTeam)
        }

        // closest
        val result = targetList.minByOrNull { it.distance(combatant.location) }!! // TODO: improve target selection
        logger.debug { "selecting closest target: $result" }
        return Pair(result, TargetSelectionStrategy.closestTarget)
    }
}