package com.vikinghelmet.dnd.dpr.scenario.combat

import com.vikinghelmet.dnd.dpr.action.*
import com.vikinghelmet.dnd.dpr.action.enums.DamageType
import com.vikinghelmet.dnd.dpr.character.PlayerCharacter
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
import com.vikinghelmet.dnd.dpr.util.DiceBlock
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalAtomicApi::class)
class AttackAction(
    val combat: Combat,
    val combatant: CombatantWithStatus,
    val battleId: Int = combat.battleId,
    val turnId: Int = combat.turnId,
    var target: CombatantWithStatus = combatant // stop hitting yourself ... initialize to self because null is messier
)
{
    var actionId = 0
    var effectId = 0
    val actionResultList = mutableListOf<CombatActionResult>()

    fun logError(msg: () -> String) = combat.logError(msg)
    fun logInfo(msg: () -> String)  = combat.logInfo(msg)
    fun logDebug(msg: () -> String) = combat.logDebug(msg)

    fun getOpponents()         = combat.getOpponents(combatant)
    fun getNotDeadOpponents()  = getOpponents().filter { !it.isDead() }.toList()
    fun getPositiveOpponents() = getOpponents().filter { it.isPositive() }.toList()

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
    fun getActionModifiersAvailable() = combatant.getActionModifiersAvailable (modifiersUsedAcrossTurns ())

    fun getClassFeatures() =
        if (combatant.combatant !is PlayerCharacter) emptyList() else combatant.combatant.getClassFeaturesEnabled()

    fun takeAction(): List<CombatActionResult> {
        logDebug { "combatant=$combatant is taking action" }
        actionId = 0
        effectId = 0

        // from here on down is all about the Attack

        val selectedTarget = chooseTarget()
        combatant.target = selectedTarget

        if (selectedTarget == null) {
            actionResultList.add (CombatActionResult(combatant, combatant, turnId, 0, 0, "no target available"))
            return actionResultList
        }

        target = selectedTarget // NOT NULL
        val attackList = chooseAttackActions().attacks

        if (attackList.isEmpty()) {
            actionResultList.add (CombatActionResult(combatant, target, turnId, 0, 0, "no attacks available for target"))
            return actionResultList
        }

        logDebug { "combatant = ${combatant.shortName()}, selected target = ${target.shortName()}" }

        val hasAdvantage   = combatant.any { it.attacksAgainstOthers == AttackAdvantage.advantage }

        // val givesAdvantage = getActionModifiersAvailable().firstOrNull { it.givesAdvantage() && !hasAdvantage }
        val givesAdvantage = getClassFeatures().firstOrNull { it.givesAdvantage() && !hasAdvantage }

        for (attack in attackList) {
            if (givesAdvantage != null) {
                logDebug { "givesAdvantage = $givesAdvantage" }
                //attack.actionModifiers.add (givesAdvantage)
                combatant.add (TargetEffect(turnId, givesAdvantage))
            }

            actionResultList.addAll (takeAttackAction (attack))

            val additionalAttack = getAdditionalAttack(attackList)
            if (additionalAttack != null) {
                target = additionalAttack.target as CombatantWithStatus
                actionResultList.addAll (takeAttackAction (additionalAttack))
            }
        }
        return actionResultList
    }

    fun takeAttackAction(attack: Attack) : List<CombatActionResult>
    {
        var attackResults = if (attack.action is Weapon) {
            meleeOrRangeAttack(attack, attack.action)
        } else {
            attackWithSpell(attack)
        }

        val result = attackResults.ifEmpty {
            listOf(CombatActionResult (combatant, target, turnId, 0, 0, "no results for attack: ${attack.action}"))
        }

        actionId++
        return result
    }

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

    fun chooseTarget(): CombatantWithStatus?
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
            logDebug { "chooseTarget: keeping current target = ${combatant.target}" }
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

    fun chooseAttackActions(): Turn {
        val preferredTurnOption =
            combatant.getPreferredTurn(ActionGoal.Attack, target, combatant.distance(target).toFeet(), combat)
                ?: return Turn(emptyList())

        if (preferredTurnOption.first.includesBA() || preferredTurnOption.first.attacks.isEmpty()) {
            return preferredTurnOption.first
        }

        val bonusMod = chooseBonusActionModifier() ?: return preferredTurnOption.first

        val attacks = preferredTurnOption.first.attacks.map { it.copy() }
        attacks[0].actionModifiers.add (bonusMod)
        return Turn(attacks)
    }

    fun chooseBonusActionModifier(): ActionModifier? {
        val bonusMod = getActionModifiersAvailable().firstOrNull { it.isBonusAction() }

        // check BA constraints, apply BA if possible, then update attack info

        when (bonusMod) {
            Rage -> {
                if (combatant.any { it.cause == Rage }) { return null } // don't add Rage if already in Rage

                combatant.temporaryDamageResistance.addAll (listOf(DamageType.bludgeoning, DamageType.piercing, DamageType.slashing))

                combatant.add (TargetEffect(turnId, cause = Rage))

                actionResultList.add (
                    CombatActionResult (combatant, combatant, turnId, "BA", 0, bonusMod.toString(), emptyList())
                )
                return null // since we create a separate line item above, don't append this mod to the attack label
            }

            // TODO: CunningAction, SteadyAim ... not ready for these yet

            SecondWind -> {
                if (combatant.currentHP > combatant.getHP() / 2) { return null } // dont use SecondWind if HP is high

                val level = (combatant.combatant as PlayerCharacter).getLevel()
                val healAmount = DiceBlock(0,0,0,1,0,level).roll()
                combatant.applyHealing(healAmount)
                actionResultList.add (
                    CombatActionResult.selfHealing (combatant, turnId, bonusMod.toString(), healAmount)
                )
                return null // since we create a separate line item above, don't append this mod to the attack label
            }

            else -> { return null }
        }

        return bonusMod
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

        return listOf (attackResult(target, attack, damageResultList))
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

        if (combatant.any { it.cause == Rage }) {
            attack.actionModifiers.add(Rage)
            damageList.add (Damage (DiceBlock(), 2, 0, baseDamageList.first().type)) // TODO: 2 -> table driven amount
        }

        if (getActionModifiersAvailable().contains(SneakAttack)) {
            // TODO: ED=1d6 (table) if you have adv and weapon is Finesse/Ranged OR if ally TS=5 and no disadv
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
                logInfo { "target(${target.shortName()}) has resistance ${it.type}, damage reduced from $effectDamage to ${effectDamage/2}" }
                effectDamage /= 2
            }
            if (target.getDamageVulnerabilities().contains(it.type)) {
                logInfo { "target(${target.shortName()}) has vulnerability ${it.type}, damage increased from $effectDamage to ${effectDamage*2}" }
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
                logInfo { "spellAttackResults is empty, spell = $spell" }
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
            logError { "castSavingThrowSpell: target list is empty (SHOULD NOT HAPPEN), focus=$focusTarget focusLoc=${focusTarget.location}, myloc=${combatant.location}" }
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

            resultList.add (attackResult (target, attack, damageResultList))
        }

        // if breath weapon or similar, add to the recharge list
        if (attack.action is SavingThrowAction && attack.action.recharge != null) {
            logInfo { "add attack to waitingForRecharge: ${attack.action}" }
            combatant.waitingForRecharge.add(attack.action)
        }

        return resultList
    }

    fun attackResult(target: CombatantWithStatus, attack: Attack, damageResultList: List<DamageResult>) = CombatActionResult(
            combatant,
            target,
            turnId,
            actionId,
            effectId++,
            attack,
            damageResultList
        )

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

                    NO_EFFECT -> {
                        logDebug { "no effect" }
                        damageResult.amount = 0
                    }

                    HALF_DAMAGE -> {
                        logDebug { "half damage" }
                        if (isEvasive) damageResult.amount = 0 else damageResult.amount /= 2
                    }

                    else -> {}
                }
            }
        }

        return damageResultList
    }
}
