package com.vikinghelmet.dnd.dpr.scenario.combat

import com.vikinghelmet.dnd.dpr.action.*
import com.vikinghelmet.dnd.dpr.action.enums.DamageType
import com.vikinghelmet.dnd.dpr.monsters.Monster
import com.vikinghelmet.dnd.dpr.scenario.TargetEffect
import com.vikinghelmet.dnd.dpr.scenario.combat.location.Cone
import com.vikinghelmet.dnd.dpr.scenario.combat.location.Direction
import com.vikinghelmet.dnd.dpr.scenario.combat.location.Distance
import com.vikinghelmet.dnd.dpr.scenario.combat.results.CombatActionResult
import com.vikinghelmet.dnd.dpr.scenario.combat.results.CombatActionResultFormatter
import com.vikinghelmet.dnd.dpr.scenario.combat.results.CombatResult
import com.vikinghelmet.dnd.dpr.scenario.combat.results.DamageResult
import com.vikinghelmet.dnd.dpr.scenario.combat.save.HealthStatus
import com.vikinghelmet.dnd.dpr.spells.SaveResult.*
import com.vikinghelmet.dnd.dpr.spells.SavingThrowAction
import com.vikinghelmet.dnd.dpr.spells.Spell
import com.vikinghelmet.dnd.dpr.spells.SpellAttack
import com.vikinghelmet.dnd.dpr.spells.payload.fields.AreaOfEffectShape
import com.vikinghelmet.dnd.dpr.util.AttackAdvantage
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
    val actionResultList = mutableListOf<CombatActionResult>()
    var turnId = 0
    var actionId = 0
    var effectId = 0

    val initialState = mutableMapOf<CombatantWithStatus, CombatActionResult>() // used for debugging
    val priorState = mutableMapOf<CombatantWithStatus, CombatActionResult>() // used during output()

    fun initInitialState() {
        for (c in (teamA+teamB)) {
            initialState[c] = CombatActionResult(c)
        }
    }

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

        initInitialState()
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

        while (teamA.any { it.isPositive() } && teamB.any { it.isPositive() }) {
            logger.info {
                "battleId=$battleId, turn=$turnId, teamA: ${ teamSummary(teamA) }, teamB: ${ teamSummary(teamB) }"
            }
            fullTurn()
            turnId++
            // exit after 100 turns, or in any aberrant situation where a live A and B occupy the same space
            val notDeadA = teamA.filter { it.isPositive() }
            val notDeadB = teamB.filter { it.isPositive() }
            val runningTooLong = turnId > 100

            while (notDeadA.any { it -> notDeadB.any { it2 -> it2.location == it.location }}) {
                notDeadA.filter { it -> notDeadB.any { it2 -> it2.location == it.location }}.forEach {
                    logger.error { "combatant in same space as opponents; jitter: $it" }
                    it.location.jitter()
                }
            }

            if (runningTooLong) {
                repeat(3) { logger.warn { "" } }
                logger.warn {"turnId=$turnId, combat is running too long" }

                logger.warn {"turnId=$turnId, teamA = ${ teamSummary(teamA)}" }
                logger.warn {"turnId=$turnId, teamB = ${ teamSummary(teamB)}" }

                initialState.forEach { (c, state) ->
                    logger.warn {
                        "turn=-1, combatant=$c, initialLoc=${state.attackerNewLocation}"
                    }
                }
                actionResultList.forEach { r ->
                    logger.warn {
                        "turn=${r.turnId}, combatant=${r.attacker}, newLoc=${r.attackerNewLocation}"
                    }
                }

                // throw IllegalStateException("turnId=$turnId, combat is running too long")
                return false
            }
        }
        if (!teamB.any { it.isPositive() }) {
            logger.warn { "battleId=$battleId, turn=$turnId, winner = teamA = ${ teamSummary(teamA) } " }
            return true
        } else {
            logger.warn { "battleId=$battleId, turn=$turnId, winner = teamB = ${ teamSummary(teamB) } " }
            return false
        }
    }

    fun isWinnerTeamA() = !teamA.all { it.isDead() }

    fun getResult() = CombatResult(this, actionResultList)

    fun teamSummary(team: List<CombatantWithStatus>): String {
        return "${ team.sortedByDescending { it.initiative }.map { it.summary() }.toList() }"
    }

    fun fullTurn() {
        if (initiativeList.isEmpty()) {
            logger.warn { "turnId=$turnId, initiative list is empty!!" }
            return
        }

        for (combatant in initiativeList)
        {
            if (combatant.isDead()) {
                logger.debug { "battleId=$battleId, fullTurn, turn=$turnId, combatant=$combatant, is dead" }
                continue
            } else if (combatant.isDying()) {
                actionResultList.add(combatant.deathSave(turnId))
                logger.info { "battleId=$battleId, fullTurn, turn=$turnId, combatant=$combatant, after death saving throw, save list: ${combatant.deathSavingThrows}, currentHP: ${combatant.currentHP}" }
                continue
            }

            combatant.checkForSaveAtStartOfTurn(turnId)

            if (!combatant.canTakeAction()) {
                val reason = if (combatant.currentHP == 0) "zeroHP" else
                    combatant.toList().filter { it.unableToAct }.toList().toString()
                actionResultList.add(CombatActionResult(combatant, combatant, turnId, 0, 0, "unable to act"))
                logger.warn { "battleId=$battleId, fullTurn, turn=$turnId, combatant=$combatant, can not take action: reason=$reason" }
            } else {
                val turnActionResults = takeTurn (combatant)
                if (turnActionResults.isEmpty()) {
                    // this should not happen ... if no action is possible, takeTurn should return a result explaining why
                    actionResultList.add(CombatActionResult(combatant, combatant, turnId, 0, 0, "able to act, but no action results"))

                    logger.warn { "battleId=$battleId, fullTurn, turn=$turnId, combatant=$combatant, action taken but NO RESULTS !!!" }
                }
                actionResultList.addAll (turnActionResults)
            }

            combatant.checkForSaveAtEndOfTurn()
        }
    }

    fun takeTurn(combatant: CombatantWithStatus): List<CombatActionResult> {
        logger.debug { "battleId=$battleId, fullTurn, turn=$turnId, combatant=$combatant is taking action" }
        actionId = 0
        effectId = 0

        val savingThrow = combatant.checkForSaveByTakingAction()
        if (savingThrow.first) {
            return listOf(CombatActionResult(combatant, combatant, turnId, 0, 0, "saving throw action, result = ${savingThrow.second}"))
        }

        val goal = combatant.getActionGoal(this)

        if (goal == ActionGoal.Heal) {
            val healTarget = chooseHealingTarget(combatant) ?:
                return listOf(CombatActionResult(combatant, combatant, turnId, 0, 0, "goal is healing, but no healing target chosen"))

            // move towards target, even if you can't heal them yet ...
            combatant.moveTowardTarget(healTarget, this)

            if (getOpponents(healTarget).any { it.location == healTarget.location }) {
                return listOf(CombatActionResult(combatant, combatant, turnId, 0, 0, "unable to heal, opponent standing on healing target"))
            }

            val range = combatant.distance(healTarget).toFeet()
            val healTurn = combatant.getPreferredTurn(goal, healTarget, range, this)

            return if (healTurn != null) {
                healWithSpell(combatant, healTarget, healTurn.first)
            } else {
                listOf(CombatActionResult(combatant, combatant, turnId, 0, 0, "goal is healing, but no healing action available"))
            }
        }

        // from here on down is all about the Attack

        val target = chooseAttackTarget(combatant)
        combatant.target = target

        if (target == null) {
            return listOf(CombatActionResult(combatant, combatant, turnId, 0, 0, "no target available"))
        }

        val attackList = chooseTurnActions(goal, combatant, target)

        if (attackList.isEmpty()) {
            return listOf(CombatActionResult(combatant, target, turnId, 0, 0, "no attacks available for target"))
        }

        logger.debug { "turn = $turnId, combatant = ${combatant.shortName()}, selected target = ${target.shortName()}" }

        val actionResults = mutableListOf<CombatActionResult>()

        for (attack in attackList) {
            var attackResults: List<CombatActionResult>

            if (attack.action is Weapon) {
                attackResults = meleeOrRangeAttack(combatant, target, attack, attack.action)
            } else {
                attackResults = attackWithSpell(combatant, target, attack)
            }

            if (attackResults.isEmpty()) {
                actionResults.add(CombatActionResult(combatant, target, turnId, 0, 0, "no results for attack: ${attack.action}"))
            } else {
                actionResults.addAll(attackResults)
            }

            actionId++
        }

        logger.info { "turn = $turnId, attackResults = $actionResults" }
        return actionResults
    }

    fun getOpponents(combatant: CombatantWithStatus) = if (combatant.onTeamA) teamB else teamA

    fun getMyTeam(combatant: CombatantWithStatus) = if (combatant.onTeamA) teamA else teamB

    fun getNotDeadOpponents(combatant: CombatantWithStatus): List<CombatantWithStatus> {
        return getOpponents(combatant).filter { !it.isDead() }.toList()
    }

    fun getPositiveOpponents(combatant: CombatantWithStatus): List<CombatantWithStatus> {
        return getOpponents(combatant).filter { it.isPositive() }.toList()
    }

    fun chooseHealingTarget(healer: CombatantWithStatus): CombatantWithStatus? {
        val team = getMyTeam(healer)

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
            return team.filter { it.isStable() }.minByOrNull { it.distance(healer) }
        }

        // if everyone is positive, ignore the undamaged, and heal someone with lowest HP
        return team.filter { it.isDamaged() }.minByOrNull { it.currentHP }
    }

    fun healWithSpell(healer: CombatantWithStatus, primaryTarget: CombatantWithStatus, turn: Turn): List<CombatActionResult> {
        val attack = turn.attacks.firstOrNull() ?: return emptyList()
        val spell = attack.action as? Spell ?: return emptyList()

        val targetsToHeal = if (spell.impactMultipleCreatures()) {
            getMyTeam(healer).filter { !it.isDead() && it.getHP() > it.currentHP } // TODO: more selective healing targets
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
            logger.info { "turn = $turnId, ${healer.shortName()} heals ${healTarget.shortName()} for $healAmount HP (now ${healTarget.currentHP}/${healTarget.getHP()})" }

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

        healer.spellCastList.add(SpellCast(healer.combatant, spell, turnId, targetList = targetsToHeal.toMutableList()))
        actionId++

        return resultList
    }

    fun chooseAttackTarget(combatant: CombatantWithStatus): CombatantWithStatus?
    {
        // if you are within melee range of a healthy opponent, you can't move (don't provoke an oppy attack) ...
        // just attack someone right in front of you
        val inMeleeRange = getPositiveOpponents(combatant).filter { combatant.distance(it) <= Distance.melee() }
        if (inMeleeRange.isNotEmpty()) {
            return TargetSelector(this, combatant, inMeleeRange).select().first  // early return because we can't move
        }

        var targetList: List<CombatantWithStatus>

        // if you already have a target that is not dead/dying, try to finish them off
        if (combatant.target != null && combatant.target!!.isPositive()) {
            logger.verbose { "chooseTarget: keeping current target = ${combatant.target}" }
            targetList = listOf(combatant.target!!)
        }
        else {
            targetList = getPositiveOpponents(combatant)
        }

        if (targetList.isEmpty()) {
            // if all you have are a few dying/stable opponents, keep sticking a fork in them until they're done
            targetList = getNotDeadOpponents(combatant)
        }
        if (targetList.isEmpty()) {
            return null // early return because we have no targets
        }

        // if you are currently someone else's target, target them back
        val attackingMeList = targetList.filter { it.canTakeAction() && it.target == combatant }.toList()
        if (!attackingMeList.isEmpty()) {
            targetList = attackingMeList
        }

        val target = TargetSelector(this, combatant, targetList).select().first ?: return null

        // now that we know we aren't in melee range, it is safe to move about the playing field as needed

        if (combatant.getPreferredCombatDistance() <= Distance.melee()) {
            combatant.moveTowardTarget(target, this)
        }
        else {
            var distance = target.distance(combatant)
            if (distance == combatant.getPreferredCombatDistance()) {
                // no movement needed
            }
            else if (distance < combatant.getPreferredCombatDistance()) { // too close for comfort
                combatant.moveAwayFromTarget(targetList, distance, this)
            }
            else {
                combatant.moveTowardTarget(target, this)
            }
        }

        return target
    }

    fun chooseTurnActions(goal: ActionGoal, combatant: CombatantWithStatus, target: CombatantWithStatus): List<Attack> {
        val preferredTurnOption = combatant.getPreferredTurn(goal, target, combatant.distance(target).toFeet(), this)
        return preferredTurnOption?.first?.attacks ?: emptyList()
    }

    fun getAttackRoll(combatant: CombatantWithStatus, target: CombatantWithStatus): Int {
        var attackRoll = (1..20).random()
        val attackAdvantage = AttackAdvantage.fromList (listOf (
            target.getAttacksAgainstMe(), combatant.getAttacksAgainstOthers()
        ))
        logger.info { "attacker advantage = $attackAdvantage" }
        when (attackAdvantage) {
            AttackAdvantage.advantage    -> attackRoll = max(attackRoll, (1..20).random())
            AttackAdvantage.disadvantage -> attackRoll = min(attackRoll, (1..20).random())
            AttackAdvantage.normal -> {}
        }
        return attackRoll
    }

    fun meleeOrRangeAttack(
        combatant: CombatantWithStatus,
        target: CombatantWithStatus,
        attack: Attack,
        action: MeleeOrRangeAction
    ) : List<CombatActionResult>
    {
        var attackRoll = getAttackRoll(combatant, target)
        val autoHit = attackRoll == 20 // critical Hit + Damage ... TODO: for a champion, autoHit on 19 or 18

        val name = action.getActionName()
        logger.debug { "combatant = $combatant, target = $target, action = $name" }

        attackRoll += action.getAttackBonus()

        combatant.forEach { attackRoll += it.attackBonus.roll() - it.attackPenalty.roll() } // bless & bane
        val damageResultList = mutableListOf<DamageResult>()

        if (attackRoll >= target.getAC() || autoHit) {
            val isCritDamage = autoHit || target.isAttackerAutoCritDamage()

            val initialDamage = getInitialDamage(attack, isCritDamage, action.getDamageList())

            damageResultList.addAll (getDamageAgainstTarget (target, initialDamage))

            target.applyDamage(turnId, damageResultList)
        }

        return listOf (CombatActionResult(combatant, target, turnId, actionId, effectId++, attack, damageResultList))
    }

    fun getInitialDamage(attack: Attack, isCrit: Boolean, baseDamageList: List<Damage>): List<DamageResult>
    {
        val damageList = baseDamageList.toMutableList()

        for (modifier in attack.actionModifiers) {
            val bonusDamage = modifier.getBonusDamage()
            if (!bonusDamage.isEmpty()) {
                damageList.add(Damage(bonusDamage.copy(), 0, 0, DamageType.undefined))
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

    fun getDamageAgainstTarget(target: CombatantWithStatus, initialDamage: List<DamageResult>)
        : List<DamageResult>
    {
        val result = mutableListOf<DamageResult>()

        for (it in initialDamage) {
            if (target.getDamageImmunities().contains(it.type)) {
                logger.info { "target has immunity ${it.type}" }
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

        logger.debug { "target = $target, damage = $result" }
        return result
    }

    fun attackWithSpell(combatant: CombatantWithStatus, target: CombatantWithStatus, attack: Attack)
        : List<CombatActionResult>
    {
        val attackBonus = combatant.getSpellBonusToHit()
        val spell = attack.action as Spell
        logger.debug { "spell = ${spell.fullString()}" }

        val result = mutableListOf<CombatActionResult>()
        for (spellAttack in spell.getSpellAttacks(attackBonus)) {
            logger.debug { "spell = ${spell.name}, spellAttack = $spellAttack" }

            if (spellAttack.isNoDamageAttack()) {
                logger.debug { "no damage" }
                continue
            }

            val spellAttackResults = if (spellAttack.isSavingThrowAttack()) {
                castSavingThrowSpell (combatant, target, spell, spellAttack, attack)
            } else {
                meleeOrRangeAttack (combatant, target, attack, spellAttack)
            }

            if (spellAttackResults.isEmpty()) {
                logger.warn { "battleId=$battleId, turn=$turnId, spellAttackResults is empty, spell = $spell" }
            }
            result.addAll(spellAttackResults)
        }

        val targetList = result.map {it.target }.toMutableList()
        //println("attackWithSpell: targetList = $targetList")

        combatant.spellCastList.add(SpellCast(combatant, spell, turnId, targetList = targetList))
        return result
    }

    fun castSavingThrowSpell(
        combatant: CombatantWithStatus,
        focusTarget: CombatantWithStatus,
        spell: Spell,
        spellAttack: SpellAttack,
        attack: Attack
    ) : List<CombatActionResult>
    {
        if (spellAttack.isNoDamageAttack()) {
            logger.warn { "This spell never directly creates damage" }
            return emptyList()
        }

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

                val potentialTargets = getPositiveOpponents(combatant).filter { points.contains(it.location) }
                val count = potentialTargets.size
                if (maxCount < count) {
                    maxCount = count
                    targetList.clear()
                    targetList.addAll (potentialTargets)
                }
            }
            logger.debug { "cone dir=$maxDir, targetList=$targetList" }
        }

        // TODO: add support for Hunters Mark damage on melee/range spell attacks

        val resultList = mutableListOf<CombatActionResult>()
        var sharedDamageList = getInitialDamage(attack, false, spellAttack.getDamageList())

        if (targetList.isEmpty()) {
            logger.warn { "battleId=$battleId, turn=$turnId, castSavingThrowSpell: target list is empty (SHOULD NOT HAPPEN), focus=$focusTarget focusLoc=${focusTarget.location}, myloc=${combatant.location}" }
        }

        for (target in targetList) {
            var successfulSave = target.makeSavingThrow (combatant.getSpellSaveDC(), save.saveAbility)
            logger.debug { "successfulSave = $successfulSave" }

            var damageResultList = getDamageAgainstTarget(target, sharedDamageList)
            var initialDamage = damageResultList.sumOf { it.amount }
            logger.debug { "initial damage = $initialDamage" }

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
            logger.info { "add attack to waitingForRecharge: ${attack.action}" }
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
        logger.debug { "saveResult (onSuccess) = ${saveResult.name}, isEvasive = $isEvasive" }

        // TODO: (almost?) all saving throw actions apply a single damage result ?

        for (damageResult in damageResultList) {
            damageResult.amount
            if (!successfulSave) {
                if (isEvasive) damage /= 2
            } else {
                when (saveResult) {
                    SPELL_ENDS -> {
                        logger.debug { "spell ends" } // TODO: update condition list ?
                        damageResult.amount = 0
                    }

                    CONDITION_ENDS -> {
                        logger.debug { "condition ends" } // TODO: update condition list ?
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

    // -----------------------------------------------------------------------
    // CSV output

    fun initPriorState() {
        priorState.clear()
        for (c in (teamA+teamB)) {
            priorState[c] = CombatActionResult(c)
        }
    }

    fun shortSummary(actionResult: CombatActionResult?): String {
        if (actionResult == null) return ""
        val target = actionResult.target

        // TODO: store location in CombatActionResult ... also, add results just for movement ...
        //val buffer = StringBuilder("(").append(target.shortName()).append(", loc=$location")

        val buffer = StringBuilder("(").append(target.shortName()).append(", ")

        if (actionResult.targetHealth == HealthStatus.positive) {
            buffer.append("hp=${actionResult.targetHP}/${target.getHP()}")
        }
        else {
            buffer.append(actionResult.targetHealth)
        }
        return buffer.append(")").toString()
    }

    fun footer(turnId: Int, label: String): String {
        val aList = teamA.map { shortSummary(priorState[it])  }.toList()
        val bList = teamB.map { shortSummary(priorState[it])  }.toList()
        return CombatActionResultFormatter.footer(battleId, turnId, label, aList, bList) +"\n"
    }

    fun output(): String {
        initPriorState()

        val buf = StringBuilder()
        buf.append(CombatActionResultFormatter.header()).append("\n")

        //buf.append (footer(0, "START OF COMBAT", false))
        for (v in initialState.values.sortedByDescending { it.attacker.initiative }) {
            buf.append (CombatActionResultFormatter.output(battleId,v)).append("\n")
        }

        var outputTurnId = 0

        for (result in actionResultList) {
            if (outputTurnId < result.turnId) {
                buf.append (footer(outputTurnId, "END OF TURN"))
                outputTurnId = result.turnId
            }
            buf.append (CombatActionResultFormatter.output(battleId, result)).append("\n")
            priorState[result.target] = result
        }

        buf.append (footer(outputTurnId, "END OF TURN"))
        return buf.toString()
    }
}
