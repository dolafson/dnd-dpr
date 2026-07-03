package com.vikinghelmet.dnd.dpr.scenario.combat

import com.vikinghelmet.dnd.dpr.action.*
import com.vikinghelmet.dnd.dpr.action.enums.DamageType
import com.vikinghelmet.dnd.dpr.character.actions.ActionModifier
import com.vikinghelmet.dnd.dpr.character.actions.ActionModifier.*
import com.vikinghelmet.dnd.dpr.character.inventory.MasteryProperty
import com.vikinghelmet.dnd.dpr.scenario.TargetEffect
import com.vikinghelmet.dnd.dpr.scenario.combat.location.Cone
import com.vikinghelmet.dnd.dpr.scenario.combat.location.Direction
import com.vikinghelmet.dnd.dpr.scenario.combat.location.Distance
import com.vikinghelmet.dnd.dpr.scenario.combat.results.CombatActionResult
import com.vikinghelmet.dnd.dpr.scenario.combat.results.DamageResult
import com.vikinghelmet.dnd.dpr.spells.SaveResult.*
import com.vikinghelmet.dnd.dpr.spells.SavingThrowAction
import com.vikinghelmet.dnd.dpr.spells.Spell
import com.vikinghelmet.dnd.dpr.spells.SpellAttack
import com.vikinghelmet.dnd.dpr.spells.payload.fields.AreaOfEffectShape
import com.vikinghelmet.dnd.dpr.util.AttackAdvantage
import com.vikinghelmet.dnd.dpr.util.Constants
import dev.shivathapaa.logger.api.LoggerFactory
import kotlinx.serialization.Transient
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalAtomicApi::class)
class CombatTurn(
    val combat: Combat,
    val combatant: CombatantWithStatus,
    val battleId: Int = combat.battleId,
    val turnId: Int = combat.turnId,
    var target: CombatantWithStatus = combatant // stop hitting yourself ... initialize to self because null is messier
)
{
    @Transient
    private val logger = LoggerFactory.get(CombatTurn::class.simpleName ?: "")

    var actionId = 0
    var effectId = 0
    val actionResultList = mutableListOf<CombatActionResult>()

    private fun pad2(input: Int) = input.toString().padStart(2, '0')
    private fun getAttrString() = mapOf("battleId" to pad2(battleId), "turnId" to pad2(turnId)).toString()
    fun logError(msg: () -> String) = logger.error { getAttrString() +": "+ msg() }
    fun logWarn(msg: () -> String)  = logger.warn  { getAttrString() +": "+ msg() }
    fun logInfo(msg: () -> String)  = logger.info  { getAttrString() +": "+ msg() }
    fun logDebug(msg: () -> String) = logger.debug { getAttrString() +": "+ msg() }

    fun fullTurn() : List<CombatActionResult> {
        if (combatant.isDead()) {
            logDebug { "combatant=$combatant, is dead" }
            return emptyList()
        } else if (combatant.isDying()) {
            actionResultList.add(combatant.deathSave(turnId))
            logInfo { "combatant=$combatant, after death saving throw, save list: ${combatant.deathSavingThrows}, currentHP: ${combatant.currentHP}" }
            return actionResultList
        }

        combatant.checkForSaveAtStartOfTurn(turnId)

        if (combatant.canTakeAction()) {
            takeAction()
        }
        else {
            val reason = if (combatant.currentHP == 0) "zeroHP" else
                combatant.toList().filter { it.unableToAct }.toList().toString()
            actionResultList.add(CombatActionResult(combatant, combatant, turnId, 0, 0, "unable to act: reason=$reason"))
        }

        combatant.checkForSaveAtEndOfTurn()
        return actionResultList
    }

    fun takeAction() {
        logDebug { "combatant=$combatant is taking action" }
        actionId = 0
        effectId = 0

        val savingThrow = combatant.checkForSaveByTakingAction()
        if (savingThrow.first) {
            actionResultList.add (CombatActionResult(combatant, combatant, turnId, 0, 0, "saving throw action, result = ${savingThrow.second}"))
            return
        }

        val goal = combatant.getActionGoal(combat)

        if (goal == ActionGoal.Heal) {
            actionResultList.addAll (takeHealingAction())
            return
        }

        // from here on down is all about the Attack

        val selectedTarget = chooseAttackTarget()
        combatant.target = selectedTarget

        if (selectedTarget == null) {
            actionResultList.add (CombatActionResult(combatant, combatant, turnId, 0, 0, "no target available"))
            return
        }

        target = selectedTarget // NOT NULL
        val attackList = chooseTurnActions(goal).attacks.toMutableList()

        if (attackList.isEmpty()) {
            actionResultList.add (CombatActionResult(combatant, target!!, turnId, 0, 0, "no attacks available for target"))
            return
        }

        logDebug { "combatant = ${combatant.shortName()}, selected target = ${target.shortName()}" }

        val hasAdvantage   = combatant.any { it.attacksAgainstOthers == AttackAdvantage.advantage }
        val givesAdvantage = getActionModifiersAvailable().firstOrNull { it.givesAdvantage() && !hasAdvantage }

        for (attack in attackList) {
            if (givesAdvantage != null) {
                attack.actionModifiers.add (givesAdvantage)
                combatant.add (TargetEffect(turnId, givesAdvantage))
            }

            actionResultList.addAll (takeAttackAction (attack))

            val additionalAttack = getAdditionalAttack(attackList)
            if (additionalAttack != null) {
                target = additionalAttack.target as CombatantWithStatus
                actionResultList.addAll (takeAttackAction (additionalAttack))
            }
        }
    }

    fun getActionModifiersAvailable() : List<ActionModifier> {
        return combatant.getActionModifiersAvailable (modifiersUsedAcrossTurns ())
    }

    fun takeHealingAction(): List<CombatActionResult> {
        val healTarget = chooseHealingTarget() ?: return listOf(
            CombatActionResult(combatant, combatant, turnId, 0, 0,
                "goal is healing, but no healing target chosen" )
        )

        // move towards target, even if you can't heal them yet ...
        combatant.moveTowardTarget(healTarget, combat)

        if (getOpponents().any { it.location == healTarget.location }) {
            return listOf(CombatActionResult(combatant, combatant, turnId, 0, 0,
                "unable to heal, opponent standing on healing target"))
        }

        // TODO: if selected healing target is not in range / unreachable (movement blocked), then either ...
        //  a) choose a closer healing target (if possible)
        //  b) perform a ranged weapon attack

        val range = combatant.distance(healTarget).toFeet()
        val healTurn = combatant.getPreferredTurn(ActionGoal.Heal, healTarget, range, combat)

        return if (healTurn != null) {
            healWithSpell(combatant, healTarget, healTurn.first)
        } else {
            listOf(CombatActionResult(combatant, combatant, turnId, 0, 0,
                "goal is healing, but no healing action available"))
        }
    }

    fun takeAttackAction(attack: Attack) : List<CombatActionResult>
    {
        var attackResults = if (attack.action is Weapon) {
            meleeOrRangeAttack(attack, attack.action)
        } else {
            attackWithSpell(attack)
        }

        val result = if (attackResults.isNotEmpty()) attackResults else
            listOf(CombatActionResult (combatant, target, turnId, 0, 0, "no results for attack: ${attack.action}"))

        actionId++
        return result
    }

    fun modifiersUsedAcrossTurns(): List<ActionModifier> { // TODO: optimize this by storing mods directly in CombatActionResult
        val result = mutableListOf<ActionModifier>()
        (combat.actionResultList + actionResultList).filter { it.attacker == combatant }.forEach { a ->
            ActionModifier.entries.forEach { mod ->
                if (a.actionTaken.contains(mod.toString())) result.add(mod)
            }
        }
        return result
    }

    fun wasModifierUsedThisTurn(mod: ActionModifier) = actionResultList.any { it.actionTaken.contains(mod.toString()) }

    fun getAdditionalAttack(attackList: List<Attack>): Attack?
    {
        val nearbyTargets = getOpponents().filter { it != target && it.isPositive() && it.distance(target).toFeet() <= Constants.MELEE_RANGE }
        if (nearbyTargets.isEmpty()) return null
        val nextTarget = nearbyTargets.first()

        val lastAttack = attackList.last()
        if (lastAttack.action !is Weapon) return null

        val lastResult = actionResultList.lastOrNull() ?: return null
        if (lastResult.damageResultList.sumOf { it.amount } == 0) return null

        combatant.getActionModifiersAvailable()
            .filter { it.givesExtraAttack() && !wasModifierUsedThisTurn(it) }
            .forEach {
                val getExtra = when (it) {
                    Cleave       -> (lastAttack.action.hasMasteryProperty(MasteryProperty.Cleave))
                    HordeBreaker -> true  // TODO: only if 2nd target not attacked by you this turn
                    SuddenStrike -> wasModifierUsedThisTurn(DreadfulStrike) // TODO: choose either SuddenStrike or MassFear
                    else -> false
                }

                if (getExtra) {
                    logDebug { "adding extra attack for ActionModifier: $it" }
                    return Attack(nextTarget, lastAttack.action, mutableListOf(it))
                }
            }
        return null
    }

    fun getOpponents() = combat.getOpponents(combatant)

    fun getMyTeam() = combat.getMyTeam (combatant)

    fun getNotDeadOpponents(): List<CombatantWithStatus> {
        return getOpponents().filter { !it.isDead() }.toList()
    }

    fun getPositiveOpponents(): List<CombatantWithStatus> {
        return getOpponents().filter { it.isPositive() }.toList()
    }

    fun chooseHealingTarget(): CombatantWithStatus? {
        val team = getMyTeam()

        // if the team has a dying cleric, heal them first (so they can heal others)
        if (team.any { it.isCleric() && it.isDying() }) {
            return team.filter { it.isDying() }.maxBy { it.deathSavingThrows.count { false }}
        }

        // choose the one closest to death ... first the dying, then the stable
        if (team.any { it.isDying() }) {
            return team.filter { it.isDying() }.maxBy { it.deathSavingThrows.count { false }}
        }

        // if no one is dying, choose the closest stable patient (if any)
        if (team.any { it.isStable() }) {
            return team.filter { it.isStable() }.minByOrNull { it.distance(combatant) }
        }

        // if everyone is positive, ignore the undamaged, and heal someone with lowest HP
        return team.filter { it.isDamaged() }.minByOrNull { it.currentHP }
    }

    fun healWithSpell(healer: CombatantWithStatus, primaryTarget: CombatantWithStatus, turn: Turn): List<CombatActionResult> {
        val attack = turn.attacks.firstOrNull() ?: return emptyList()
        val spell = attack.action as? Spell ?: return emptyList()

        val targetsToHeal = if (spell.impactMultipleCreatures()) {
            getMyTeam().filter { !it.isDead() && it.getHP() > it.currentHP } // TODO: more selective healing targets
        } else {
            listOf(primaryTarget)
        }

        // TODO: filter healing targets by spell range

        val resultList = mutableListOf<CombatActionResult>()
        val healAmountRolled = healer.getHealingAmount(spell, true)

        for (healTarget in targetsToHeal) {
            var healAmount = healAmountRolled
            if (spell.impactMultipleCreatures()) {
                healAmount /= targetsToHeal.size    // TODO: support uneven distribution of healing amount
            }
            healTarget.applyHealing(healAmount)
            logInfo { "${healer.shortName()} heals ${healTarget.shortName()} for $healAmount HP (now ${healTarget.currentHP}/${healTarget.getHP()})" }

            val damageResultList = listOf(DamageResult(healAmount, DamageType.healing))
            resultList.add (CombatActionResult(
                healer,
                healTarget,
                turnId,
                actionId,
                effectId++,
                attack,
                damageResultList
            ))
        }

        healer.recordSpellCasting(spell, turnId, targetsToHeal)
        actionId++

        return resultList
    }

    fun chooseAttackTarget(): CombatantWithStatus?
    {
        // if you are within melee range of a healthy opponent, you can't move (don't provoke an oppy attack) ...
        // just attack someone right in front of you
        val inMeleeRange = getPositiveOpponents().filter { combatant.distance(it) <= Distance.melee() }
        if (inMeleeRange.isNotEmpty()) {
            return TargetSelector(combat, combatant, inMeleeRange).select().first  // early return because we can't move
        }

        var targetList: List<CombatantWithStatus>

        // if you already have a target that is not dead/dying, try to finish them off
        if (combatant.target != null && combatant.target!!.isPositive()) {
            logger.verbose { "chooseTarget: keeping current target = ${combatant.target}" }
            targetList = listOf(combatant.target!!)
        }
        else {
            targetList = getPositiveOpponents()
        }

        if (targetList.isEmpty()) {
            // if all you have are a few dying/stable opponents, keep sticking a fork in them until they're done
            targetList = getNotDeadOpponents()
        }
        if (targetList.isEmpty()) {
            return null // early return because we have no targets
        }

        // if you are currently someone else's target, target them back
        val attackingMeList = targetList.filter { it.canTakeAction() && it.target == combatant }.toList()
        if (!attackingMeList.isEmpty()) {
            targetList = attackingMeList
        }

        val target = TargetSelector(combat, combatant, targetList).select().first ?: return null

        // now that we know we aren't in melee range, it is safe to move about the playing field as needed

        if (combatant.getPreferredCombatDistance() <= Distance.melee()) {
            combatant.moveTowardTarget(target, combat)
        }
        else {
            var distance = target.distance(combatant)
            if (distance == combatant.getPreferredCombatDistance()) {
                // no movement needed
            }
            else if (distance < combatant.getPreferredCombatDistance()) { // too close for comfort
                combatant.moveAwayFromTarget(targetList, distance, combat)
            }
            else {
                combatant.moveTowardTarget(target, combat)
            }
        }

        return target
    }

    fun chooseTurnActions(goal: ActionGoal): Turn {
        val preferredTurnOption = combatant.getPreferredTurn(goal, target, combatant.distance(target).toFeet(), combat)
        return preferredTurnOption?.first ?: Turn(emptyList())
    }

    fun getAttackRoll(): Int {
        var attackRoll = (1..20).random()
        val attackAdvantage = AttackAdvantage.fromList (listOf (
            target.getAttacksAgainstMe(), combatant.getAttacksAgainstOthers()
        ))
        logInfo { "attacker advantage = $attackAdvantage" }
        when (attackAdvantage) {
            AttackAdvantage.advantage    -> attackRoll = max(attackRoll, (1..20).random())
            AttackAdvantage.disadvantage -> attackRoll = min(attackRoll, (1..20).random())
            AttackAdvantage.normal -> {}
        }
        return attackRoll
    }

    fun meleeOrRangeAttack(
        attack: Attack,
        action: MeleeOrRangeAction
    ) : List<CombatActionResult>
    {
        var attackRoll = getAttackRoll()
        val autoHit = attackRoll == 20 // critical Hit + Damage ... TODO: for a champion, autoHit on 19 or 18

        val name = action.getActionName()
        logDebug { "combatant = $combatant, target = $target, action = $name" }

        attackRoll += action.getAttackBonus()

        combatant.forEach { attackRoll += it.attackBonus.roll() - it.attackPenalty.roll() } // bless & bane
        val damageResultList = mutableListOf<DamageResult>()

        if (attackRoll >= target.getAC() || autoHit) {
            val isCritDamage  = autoHit || target.isAttackerAutoCritDamage()
            val initialDamage = getDamage(attack, isCritDamage, action.getDamageList())

            damageResultList.addAll (applyDamageImmunityResistanceAndVulnerability (target, initialDamage))

            target.applyDamage(turnId, damageResultList)
        }

        return listOf (CombatActionResult(combatant, target, turnId, actionId, effectId++, attack, damageResultList))
    }

    fun getDamage(attack: Attack, isCrit: Boolean, baseDamageList: List<Damage>): List<DamageResult>
    {
        val damageList = baseDamageList.toMutableList()

        if (attack.action is Weapon) {
            getActionModifiersAvailable()
                .filter { it.onWeaponHit() }
                .filter { ! wasModifierUsedThisTurn(it) }
                .filter { it.getDamage().isNotEmpty() }
                .forEach {
                    attack.actionModifiers.add(it)
                    damageList.add(it.getDamage())
                }
        }

        val result = mutableListOf<DamageResult>()
        for (it in damageList) {
            val roll = it.dice.roll()
            var effectDamage = (if (isCrit) roll * 2 else roll) + it.bonus
            if (attack.isBonusAction != true) {
                effectDamage += it.abilityBonus
            }
            result.add (DamageResult (effectDamage, it.type))
        }

        return result
    }

    fun applyDamageImmunityResistanceAndVulnerability (target: CombatantWithStatus, initialDamage: List<DamageResult>)
        : List<DamageResult>
    {
        val result = mutableListOf<DamageResult>()

        for (it in initialDamage) {
            if (target.getDamageImmunities().contains(it.type)) {
                logInfo { "target has immunity ${it.type}" }
                continue
            }
            var effectDamage = it.amount
            if (target.getDamageResistances().contains(it.type)) {
                effectDamage /= 2
            }
            if (target.getDamageVulnerabilities().contains(it.type)) {
                effectDamage *= 2
            }
            result.add (DamageResult (effectDamage, it.type))
        }

        logDebug { "target = $target, damage = $result" }
        return result
    }

    fun attackWithSpell(attack: Attack) : List<CombatActionResult>
    {
        val attackBonus = combatant.getSpellBonusToHit()
        val spell = attack.action as Spell
        logDebug { "spell = ${spell.fullString()}" }

        val result = mutableListOf<CombatActionResult>()
        for (spellAttack in spell.getSpellAttacks(attackBonus)) {
            logDebug { "spell = ${spell.name}, spellAttack = $spellAttack" }

            val spellAttackResults = if (spellAttack.isSavingThrowAttack()) {
                castSavingThrowSpell (spell, spellAttack, attack)
            } else {
                meleeOrRangeAttack (attack, spellAttack)
            }

            if (spellAttackResults.isEmpty()) {
                logWarn { "spellAttackResults is empty, spell = $spell" }
            }
            result.addAll(spellAttackResults)
        }

        val targetList = result.map {it.target }
        //println("attackWithSpell: targetList = $targetList")

        combatant.recordSpellCasting(spell, turnId, targetList)
        return result
    }

    fun castSavingThrowSpell(
        spell: Spell,
        spellAttack: SpellAttack,
        attack: Attack
    ) : List<CombatActionResult>
    {
        val focusTarget = target
        val save = spellAttack.attackPayload.save!!

        val targetList = mutableListOf<CombatantWithStatus>()
        var totalDamage = 0

        val aoe = spellAttack.getAoe()

        if (aoe == null || aoe.shape != AreaOfEffectShape.Cone) { // TODO: add support for other shapes
            targetList.add(focusTarget)
        }
        else {
            var maxCount=0
            var maxDir: Direction? = null
            for (dir in Direction.entries) {
                val cone = Cone(combatant.location, dir, aoe.getSizeInFeet())
                val points = cone.getPoints()
                if (! points.contains(focusTarget.location)) continue

                val potentialTargets = getPositiveOpponents().filter { points.contains(it.location) }
                val count = potentialTargets.size
                if (maxCount < count) {
                    maxCount = count
                    targetList.clear()
                    targetList.addAll (potentialTargets)
                }
            }
            logDebug { "cone dir=$maxDir, targetList=$targetList" }
        }

        // TODO: add support for Hunters Mark damage on melee/range spell attacks

        val resultList = mutableListOf<CombatActionResult>()
        var sharedDamageList = getDamage(attack, false, spellAttack.getDamageList())

        if (targetList.isEmpty()) {
            logWarn { "castSavingThrowSpell: target list is empty (SHOULD NOT HAPPEN), focus=$focusTarget focusLoc=${focusTarget.location}, myloc=${combatant.location}" }
        }

        for (target in targetList) {
            var successfulSave = target.makeSavingThrow (combatant.getSpellSaveDC(), save.saveAbility)
            logDebug { "successfulSave = $successfulSave" }

            var damageResultList = applyDamageImmunityResistanceAndVulnerability(target, sharedDamageList)
            var initialDamage = damageResultList.sumOf { it.amount }
            logDebug { "initial damage = $initialDamage" }

            val finalDamage = applySavingThrowDamageModifiers(spellAttack, attack, initialDamage, successfulSave, damageResultList)
            target.applyDamage(turnId, finalDamage)

            totalDamage += finalDamage.sumOf { it.amount }

            if (!successfulSave) {
                val effect = TargetEffect(turnId, spell, save = save, spellSaveDC = combatant.getSpellSaveDC())
                if (!effect.isEmpty()) {
                    target.add(effect)
                }
            }

            resultList.add (CombatActionResult(
                combatant,
                target,
                turnId,
                actionId,
                effectId++,
                attack,
                damageResultList
            ))
        }

        // if breath weapon or similar, add to the recharge list
        if (attack.action is SavingThrowAction && attack.action.recharge != null) {
            logInfo { "add attack to waitingForRecharge: ${attack.action}" }
            combatant.waitingForRecharge.add(attack.action)
        }

        return resultList
    }

    fun applySavingThrowDamageModifiers(
        spellAttack: SpellAttack,
        attack: Attack,
        initialDamage: Int,
        successfulSave: Boolean,
        damageResultList: List<DamageResult>
    ): List<DamageResult> {
        var damage = initialDamage
        val saveResult = spellAttack.getSaveResult()
        val isEvasive = attack.target.isEvasive()
        logDebug { "saveResult (onSuccess) = ${saveResult.name}, isEvasive = $isEvasive" }

        // TODO: (almost?) all saving throw actions apply a single damage result ?

        for (damageResult in damageResultList) {
            damageResult.amount
            if (!successfulSave) {
                if (isEvasive) damage /= 2
            } else {
                when (saveResult) {
                    SPELL_ENDS -> {
                        logDebug { "spell ends" } // TODO: update condition list ?
                        damageResult.amount = 0
                    }

                    CONDITION_ENDS -> {
                        logDebug { "condition ends" } // TODO: update condition list ?
                        damageResult.amount = 0
                    }

                    NO_EFFECT -> damageResult.amount = 0
                    HALF_DAMAGE -> {
                        if (isEvasive) damageResult.amount = 0 else damageResult.amount /= 2
                    }

                    else -> {}
                }
            }
        }

        return damageResultList
    }
}
