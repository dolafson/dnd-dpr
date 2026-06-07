package com.vikinghelmet.dnd.dpr.scenario.combat

import com.vikinghelmet.dnd.dpr.action.*
import com.vikinghelmet.dnd.dpr.action.enums.DamageType
import com.vikinghelmet.dnd.dpr.monsters.Monster
import com.vikinghelmet.dnd.dpr.scenario.onesided.Scenario
import com.vikinghelmet.dnd.dpr.spells.SaveResult.*
import com.vikinghelmet.dnd.dpr.spells.SavingThrowAction
import com.vikinghelmet.dnd.dpr.spells.SpellAttack
import dev.shivathapaa.logger.api.LoggerFactory
import kotlinx.serialization.Transient
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.fetchAndIncrement

class Combat() {
    @Transient
    private val logger = LoggerFactory.get(Combat::class.simpleName ?: "")
    val teamA = mutableListOf<CombatantWithStatus>()
    val teamB = mutableListOf<CombatantWithStatus>()
    var initiativeList = listOf<CombatantWithStatus>()
    val combatActionList = mutableListOf<CombatAction>()
    var turn = 0
    val lastScenario = mutableMapOf<CombatantWithStatus, Scenario>()

    @OptIn(ExperimentalAtomicApi::class)
    constructor(noStatusTeamA: List<Combatant>, noStatusTeamB: List<Combatant>) : this() {
        fun getCounter(team: List<Combatant>): AtomicInt? {
            return if (team.size > 1 && team.all { it == team.firstOrNull() }) AtomicInt(0) else null
        }

        val aCounter = getCounter(noStatusTeamA)
        val bCounter = getCounter(noStatusTeamB)

        fun getNewName(combatant: Combatant, teamCounter: AtomicInt?): String {
            if (combatant is Monster) {
                return combatant.getName().replace(" ".toRegex(), "") // just eliminate whitespace
            }
            // PC: eliminate everything after+including first whitespace
            val tmp = combatant.getName().replace(" .*".toRegex(), "")
            return if (teamCounter == null) tmp else "$tmp ${'A' + teamCounter.fetchAndIncrement()}"
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
            logger.info {
                "turn=$turn, teamA: ${
                    teamA.map { it.summary() }.toList()
                }, teamB: ${teamB.map { it.summary() }.toList()}"
            }
            fullTurn()
            turn++
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
                logger.debug { "fullTurn, turn=$turn, combatant=$combatant, is dead" }
            } else if (combatant.isDying()) {
                // TODO: roll for death saving throw
                combatant.deathSave()
                logger.debug { "fullTurn, turn=$turn, combatant=$combatant, after death saving throw, save list: ${combatant.deathSavingThrows}, currentHP: ${combatant.currentHP}" }
            } else if (!combatant.canTakeAction()) {
                logger.info { "fullTurn, turn=$turn, combatant=$combatant, can not take action" }
            } else {
                logger.info { "fullTurn, turn=$turn, combatant=$combatant is taking action" }
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
                meleeOrRangeAttack(combatant, target, attack, attack.action)
            } else {
                attackWithSpell(combatant, target, attack)
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
        targetList.forEach {
            if (it.target == combatant) {
                logger.debug { "choosing a new target that is already attacking: ${combatant.target!!}" }
                return it
            }
        }

        // if you are within melee range, you can't move (don't provoke an oppy attack) ...
        // might as well attack
        val inMeleeRange = targetList.any { combatant.distance(it) <= Distance.melee() }
        if (inMeleeRange) {
            val result = targetList.filter { combatant.distance(it) <= Distance.melee() }
                .random() // TODO: improve target selection
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
                .append(", closestDistance = $closestDistance")
            logger.debug { buf.toString() }
        }

        if (preferredDistance <= Distance.melee()) {
            // pick a target, then move towards it
            combatant.moveTowardTarget(closest)    // TODO: improve target selection
            closestDistance = closest.distance(combatant)
            logMovement("moving toward melee target $closest")
            return closest
        }

        // if you are too close for comfort ... run away before picking a target
        if (closestDistance <= preferredDistance) {
            closestDistance = combatant.moveAwayFromTarget(targetList, closestDistance)
            logMovement("moving away from targets")
        }

        val result = targetList.minByOrNull { it.distance(combatant.location) }!! // TODO: improve target selection
        logger.debug { "choosing a new target: $result" }
        return result
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
                combatant.spellCastList.add(SpellCast(combatant, spell, turn))
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

    fun meleeOrRangeAttack(combatant: CombatantWithStatus, target: CombatantWithStatus, attack: Attack, action: MeleeOrRangeAction) {
        var attackRoll = getAttackRoll(combatant, target)
        val name = action.getActionName()
        logger.debug { "combatant = $combatant, target = $target, action = $name" }

        // TODO: bless and bane, maybe others?
        //var bonusDiceToHit: DiceBlock = DiceBlock(0, 0, 0, 0, 0)
        //var penaltyDiceToHit: DiceBlock = DiceBlock(0, 0, 0, 0, 0)

        attackRoll += action.getAttackBonus()

        val autoHit = attackRoll == 20 // critical Hit + Damage ... TODO: for a champion, autoHit on 19 or 18

        if (attackRoll >= target.getAC() || autoHit) {
            val isCrit = autoHit || target.attackerAutoCrit
            val damage = computeDamage(attack, target, isCrit, action.getDamageList())
            target.currentHP -= damage
        }
    }

    fun computeDamage(attack: Attack, target: CombatantWithStatus, isCrit: Boolean, baseDamageList: List<Damage>): Int
    {
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
            var effectDamage = (if(isCrit) roll*2 else roll) + it.bonus
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

    fun attackWithSpell(combatant: CombatantWithStatus, target: CombatantWithStatus, attack: Attack) {
        val attackBonus = combatant.getSpellBonusToHit()
        val spell = attack.action as com.vikinghelmet.dnd.dpr.spells.Spell
        logger.debug { "spell = ${spell.fullString()}" }

        for (spellAttack in spell.getSpellAttacks(attackBonus)) {
            logger.debug { "spell = ${spell.name}, spellAttack = $spellAttack" }

            if (spellAttack.isNoDamageAttack()) {
                logger.debug { "no damage" }
                continue
            }

            if (spellAttack.isSavingThrowAttack()) {
                castSavingThrowSpell (combatant, target, spellAttack, attack)
            } else {
                meleeOrRangeAttack (combatant, target, attack, spellAttack)
            }
        }
    }

    fun castSavingThrowSpell(combatant: CombatantWithStatus, target: CombatantWithStatus, spellAttack: SpellAttack,  attack: Attack)
    {
        if (spellAttack.isNoDamageAttack()) {
            logger.debug  { "This spell never directly creates damage" }
            return
        }

        val save = spellAttack.attackPayload.save!!

        // TODO: area of effect spells (multiple targets)

        // TODO: add support for Hunters Mark damage on melee/range spell attacks

        var successfulSave = false

        if (! combatant.autoFailSave.contains(save.saveAbility)) {
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

        target.currentHP -= applySavingThrowDamageModifiers(spellAttack, attack, initialDamage, successfulSave)

        // TODO: on a failed save add conditions to target

        // if breath weapon or similar, add to the recharge list
        if (combatant.combatant is Monster && attack.action is SavingThrowAction) {
            logger.info { "add attack to waitingForRecharge: ${attack.action}"}
            combatant.combatant.waitingForRecharge.add(attack.action)
        }
    }

    fun applySavingThrowDamageModifiers(spellAttack: SpellAttack, attack: Attack, initialDamage: Int, successfulSave: Boolean): Int
    {
        var damage = initialDamage
        val saveResult = spellAttack.getSaveResult()
        val isEvasive = attack.target.isEvasive()
        logger.debug { "saveResult (onSuccess) = ${saveResult.name}, isEvasive = $isEvasive" }

        if (!successfulSave) {
            if (isEvasive) damage /= 2
        }
        else {
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
                HALF_DAMAGE -> { if (isEvasive) damage = 0 else damage /= 2 }
                else -> {}
            }
        }

        logger.debug { "final damage = $damage" }
        return damage
    }

}