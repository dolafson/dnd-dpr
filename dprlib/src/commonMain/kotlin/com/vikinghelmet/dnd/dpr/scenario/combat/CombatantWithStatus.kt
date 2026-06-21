package com.vikinghelmet.dnd.dpr.scenario.combat

import com.vikinghelmet.dnd.dpr.action.Combatant
import com.vikinghelmet.dnd.dpr.action.Turn
import com.vikinghelmet.dnd.dpr.action.enums.DamageType
import com.vikinghelmet.dnd.dpr.character.PlayerCharacter
import com.vikinghelmet.dnd.dpr.character.classes.ClassName
import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import com.vikinghelmet.dnd.dpr.monsters.Monster
import com.vikinghelmet.dnd.dpr.scenario.TargetEffect
import com.vikinghelmet.dnd.dpr.scenario.TargetEffectCause
import com.vikinghelmet.dnd.dpr.scenario.TargetEffectList
import com.vikinghelmet.dnd.dpr.scenario.combat.location.Distance
import com.vikinghelmet.dnd.dpr.scenario.combat.location.Location
import com.vikinghelmet.dnd.dpr.scenario.combat.results.CombatActionResult
import com.vikinghelmet.dnd.dpr.scenario.combat.results.DamageResult
import com.vikinghelmet.dnd.dpr.scenario.combat.save.*
import com.vikinghelmet.dnd.dpr.scenario.onesided.ScenarioBuilder
import com.vikinghelmet.dnd.dpr.spells.Spell
import com.vikinghelmet.dnd.dpr.spells.SpellsWithComplexRules.HuntersMark
import com.vikinghelmet.dnd.dpr.util.Condition
import com.vikinghelmet.dnd.dpr.util.Constants
import com.vikinghelmet.dnd.dpr.util.Constants.levelToFavoredEnemyMap
import com.vikinghelmet.dnd.dpr.util.Globals
import com.vikinghelmet.dnd.dpr.util.Movement
import dev.shivathapaa.logger.api.LoggerFactory
import kotlinx.serialization.Transient
import kotlin.math.max
import kotlin.math.min

data class CombatantWithStatus(
    val combatant: Combatant,
    val newName: String,
    val onTeamA: Boolean,
    val turn: Int = 0,
    var location: Location = Location(onTeamA),
    var currentHP: Int = combatant.getHP(),
    var initiative: Int = (1..20).random() + combatant.getInitiativeBonus(),
    var flightSupported: Boolean = false,
) : Combatant by combatant, TargetEffectList() {

    @Transient
    private val logger = LoggerFactory.get(CombatantWithStatus::class.simpleName ?: "")

    val deathSavingThrows = mutableListOf<Boolean>()
    val spellCastList = mutableListOf<SpellCast>()

    var target: CombatantWithStatus? = null

    var healthStatus: HealthStatus = HealthStatus.positive

    val temporaryDamageResistance = mutableListOf<DamageType>()
    val temporaryDamageImmunity = mutableListOf<DamageType>()
    val temporaryDamageVulnerability = mutableListOf<DamageType>()

    var savingThrowGenerator = SavingThrowGenerator(this)

    fun canTakeAction() = currentHP > 0 && !isUnableToAct()

    override fun getDamageImmunities() = combatant.getDamageImmunities() + temporaryDamageImmunity
    override fun getDamageResistances() = combatant.getDamageResistances() + temporaryDamageResistance
    override fun getDamageVulnerabilities() = combatant.getDamageVulnerabilities() + temporaryDamageVulnerability

    fun shortName() = newName // getName().replace(" .*".toRegex(), "")

    // override fun toString() = getName()
    override fun toString() = shortName()

    fun toFullString(): String {
        return "CombatantWithStatus(combatant=$combatant, onTeamA=$onTeamA, turn=$turn, location=$location, currentHP=$currentHP, deathSavingThrows=$deathSavingThrows, spellCastList=$spellCastList, target=$target)"
    }

    fun summary(): String {
        val buffer = StringBuilder("(").append(shortName()).append(", loc=$location")
        buffer.append(", ").append(healthStatus)
        when (healthStatus) {
            HealthStatus.dying -> {
                val failCount = deathSavingThrows.filter { !it }.count()
                val passCount = deathSavingThrows.filter { it }.count()
                buffer.append(", saves=-$failCount/+$passCount")
            }
            HealthStatus.positive -> {
                buffer.append(", hp=$currentHP/${getHP()}")
            }
            else -> {}
        }
        return buffer.append(")").toString()
    }

    // --------------------------------------------------------------------
    // DEATH

    fun isDeadOrDying() = listOf(HealthStatus.dead, HealthStatus.dying).contains(healthStatus)

    fun isPositive() = healthStatus == HealthStatus.positive
    fun isDead() = healthStatus == HealthStatus.dead

    fun isDying() = healthStatus == HealthStatus.dying
    fun isStable() = healthStatus == HealthStatus.stable
    fun isDamaged() = healthStatus == HealthStatus.positive && getHP() > currentHP

    fun startDying(turnId: Int, cause: TargetEffectCause) {
        if (combatant is Monster) { // monsters dont get death saves
            die(turnId)
            return
        }
        currentHP = 0
        healthStatus = HealthStatus.dying
        // add the Unconscious if not there already
        if (! (any { it.cause != null && it.cause is DamageResult } )) {
            add(TargetEffect(turnId, cause = cause, conditions = mutableListOf(Condition.Unconscious)))
        }
    }

    fun die(turnId: Int) {
        currentHP = 0
        healthStatus = HealthStatus.dead
        breakConcentration(turnId)
    }

    fun deathSave(turnId: Int): CombatActionResult {
        val saveRoll = (1..20).random()
        var label = ""
        if (saveRoll == 1) {
            deathSavingThrows.add(false)
            deathSavingThrows.add(false)
            label = "double fail"
        } else if (saveRoll < 10) {
            deathSavingThrows.add(false)
            label = "fail"
        } else if (saveRoll < 20) {
            deathSavingThrows.add(true)
            label = "success"
        }
        else if (saveRoll == 20) {
            label = "crit success, revived"
            stabilize(1)
        }

        if (deathSavingThrows.filter { it == true }.count() >= 3) {
            deathSavingThrows.clear()
            stabilize(0)
            label += ", stable"
        }

        if (deathSavingThrows.filter { it == false }.count() >= 3) {
            label += ", died"
            die(turnId)
        }

        return CombatActionResult(
            this,
            this,
            turnId,
            "0",
            0,
            "deathSave: $label",
            emptyList(),
            currentHP,
            healthStatus,
            CombatActionResult.toDeathSaves(deathSavingThrows),
            getEffectString(),
            getConditionString(),
            this.location
        )
    }

    // --------------------------------------------------------------------
    // LOCATIONS, DISTANCE, MOVEMENT

    fun distance(target: CombatantWithStatus) =  distance(target.location)

    fun distance(otherLocation: Location) = otherLocation.distance(location)

    fun getSpeed() = if (flightSupported) max(getSpeed(Movement.fly),getSpeed(Movement.walk)) else getSpeed(Movement.walk)

    fun moveAwayFromTarget(targetList: List<CombatantWithStatus>, closestDistanceStart: Distance): Distance {
        var closestDistance = closestDistanceStart
        val initialLoc = location.copy()
        val maxMoves = getSpeed() / Constants.DISTANCE_GRANULARITY
        val targetLocationList = targetList.map { it.location }.toList()

        for (i in 1..maxMoves) {
            val oneOffMap = mutableMapOf<Location, Distance>()

            for (oneOffLoc in location.getOneOff()) {
                val distanceList = mutableListOf<Distance>()
                targetLocationList.forEach {
                    val d = it.distance(oneOffLoc)
                    if (d <= closestDistance) {
                        logger.verbose { "at least one target is closer/equal at this position, do not move here: $oneOffLoc" }
                        continue
                    }
                    else distanceList.add(d)
                }
                oneOffMap[oneOffLoc] = distanceList.minBy { it.units } // shortest distance to any target
            }

            if (oneOffMap.isEmpty()) {
                break
            }

            // choose the max of the mins
            location        = oneOffMap.maxBy { it.value }.key
            closestDistance = oneOffMap.maxBy { it.value }.value
        }

        logMovement("moving away from targets", initialLoc, closestDistance)
        return closestDistance
    }

    fun moveTowardTarget(target: CombatantWithStatus, combat: Combat): Distance {
        val initialLoc = location.copy()
        // you can not occupy the same space as another creature ... unless they are dead
        val locationsToAvoid = (combat.teamA + combat.teamB).filter { !it.isDead() }.map { it.location }

        location.moveTowardLocation(target.location, getSpeed() / Constants.DISTANCE_GRANULARITY, locationsToAvoid)

        val distance = distance(target.location)
        logMovement("moving toward target $target", initialLoc, distance)
        return distance
    }

    fun getPreferredCombatDistance(): Distance {
        val hasBetterDexThanStr = getAbilityModifier(AbilityType.Dexterity) >= getAbilityModifier(AbilityType.Strength)
        val range = if (hasBetterDexThanStr) {
            min(60, getWeaponList().maxOf { it.range }) // TODO: we don't want to be too far from our own group ...
        } else Constants.MELEE_RANGE
        return Distance.fromFeet(range)
    }

    fun getMaxRange(): Int {
        return getWeaponList().maxOf { it.range }
    }

    fun logMovement(toOrFrom: String, oldLocation: Location, newDistance: Distance) {
        val start = Globals.rightPad("${shortName()}: $toOrFrom",45)
        val buf = StringBuilder(start)
            .append(", oldLoc = $oldLocation")
            .append(", newLoc = $location")
            .append(", preferredDistance = ${ getPreferredCombatDistance() }")
            .append(", closestDistance = $newDistance")
        logger.debug { buf.toString() }
    }

    // --------------------------------------------------------------------
    // SPELLS

    fun isSlotAvailable(spell: Spell): Boolean {
        val level = spell.properties.Level
        if (level == 0) return true // cantrip

        if (spellCastList.isEmpty()) return true

        if (combatant is PlayerCharacter && spell.name == HuntersMark.getNameWithWS()) {
            val maxSlots = levelToFavoredEnemyMap[combatant.getLevel()] ?: return false
            val slotsUsed = spellCastList.count { it.spell.name == spell.name }
            return (slotsUsed < maxSlots)
        }

        if (combatant.getSpellSlots().isEmpty()) return true

        if (level < 0) return true // SavingThrowAction such as Breath Weapon

        val maxSlots = combatant.getSpellSlots()[level - 1]
        val slotsUsed =
            spellCastList.count { it.spell.properties.Level == spell.properties.Level && it.spell.name != HuntersMark.getNameWithWS() }
        if (slotsUsed >= maxSlots) Globals.debug("not enough slots: level=$level, slotsUsed=$slotsUsed, max=$maxSlots, spellsUsed = $spellCastList")
        return (slotsUsed < maxSlots)
    }

    // --------------------------------------------------------------------
    // SAVING THROWS

    fun makeSavingThrow (spellSaveDC: Int, saveAbility: AbilityType): Boolean  {
        return savingThrowGenerator.makeSavingThrow(spellSaveDC, saveAbility)
    }

    fun checkForSaveAtStartOfTurn(turnId: Int) {
        // check spells cast on others - if expired duration, remove the effect from all targets
        spellCastList.filter { it.isExpired(turnId) }.forEach { spellCast ->
            spellCast.targetList.forEach { target ->
                target.removeAll {
                    it.cause === spellCast.spell
                }
            }
        }

        savingThrowGenerator.makeSavingThrows (SaveAtStartOfTurn::contains)
    }

    fun checkForSaveAtEndOfTurn() {
        savingThrowGenerator.makeSavingThrows (SaveAtEndOfTurn::contains)
    }
    fun checkForSaveByTakingAction(): Boolean {
        return savingThrowGenerator.makeSavingThrows (SaveByTakingAnAction::contains, true)
    }

    // --------------------------------------------------------------------
    // DAMAGE and HEALING

    fun applyDamage(turnId: Int, damageResultList: List<DamageResult>) {
        val totalDamage = damageResultList.sumOf { it.amount }

        // if Sleep spell was cast, cancel it here
        val effectIterator = iterator()
        effectIterator.forEach { if (it.cause.toString() == "Sleep") {
            logger.debug { "Sleep cancelled by attack damage" }
            effectIterator.remove()
        }}

        if (totalDamage == 0) {
            return
        }
        else if (currentHP > totalDamage) {
            currentHP -= totalDamage
        }
        else if (totalDamage - currentHP > getHP()) {
            logger.debug { "instant death: totalDamage=$totalDamage, currentHP=$currentHP, maxHP=${getHP()}" }
            die(turnId)
        }
        else {
            startDying(turnId, damageResultList.first());
        }

        if (spellCastList.isEmpty() || !spellCastList.any { it.isStillRunning() }) {
            return
        }

        // spellcaster: when taking damage, make a saving throw to maintain concentration
        val saveDC = min (10, totalDamage/2)
        val savingThrowSuccess = makeSavingThrow (saveDC, AbilityType.Constitution)
        if (!savingThrowSuccess) {
            breakConcentration (turnId)
        }
    }

    fun breakConcentration(turnId: Int) {
        logger.debug { "concentration is broken" }

        spellCastList.filter { it.isStillRunning() }.forEach { spellCast ->
            spellCast.turnEnded = turnId
            spellCast.targetList.forEach { spellTarget ->
                spellTarget.removeAll {
                    it.cause === spellCast.spell
                }
            }
        }
    }

    fun isCleric(): Boolean {
        return (combatant is PlayerCharacter) && (combatant.getClass() == ClassName.Cleric)
    }

    fun rollHealingAmount(spell: Spell): Int {
        val healing = spell.getHealing()
        var roll = healing.healingDice.roll()

        if (healing.ability == "auto") {
            val spellCastingBonus = (this.combatant as? PlayerCharacter)?.getSpellAbilityBonusWithoutPB() ?: 0
            roll += spellCastingBonus
        }
        // TODO: tempHP
        return roll
    }

    fun stabilize(newHP: Int) {
        deathSavingThrows.clear()
        currentHP = newHP
        healthStatus = if (newHP > 0) HealthStatus.positive else HealthStatus.stable
        if (newHP > 0) {
            removeAll { it.cause != null && it.cause is DamageResult }
        }
    }

    fun applyHealing(healAmount: Int) {
        val newHP = min(currentHP + healAmount, getHP())
        stabilize(newHP)
    }

    // --------------------------------------------------------------------
    // TURN OPTIONS

    fun getPossibleTurns(target: CombatantWithStatus, range: Int): List<Turn> {
        val builder = ScenarioBuilder(this, target)

        val possibleTurns = builder.possibleTurns(this.getActionsAvailable(), range).toMutableList()

        val iterator = possibleTurns.iterator()
        while (iterator.hasNext()) {
            val turn = iterator.next()
            // println("getPossibleTurns: turn=$turn")

            for (attack in turn.attacks) if (attack.action is Spell) {
                if (!this.isSlotAvailable(attack.action)) {
                    logger.debug { "no slots available for spell = ${attack.action.name}" }
                    iterator.remove()
                }
                else if (spellCastList.any { it.isStillRunning() && it.spell.name == attack.action.name })
                {
                    logger.debug { "spell cast previously and still running = ${attack.action.name}" }
                    iterator.remove()
                }
            }
        }
        return possibleTurns
    }

    fun getPreferredTurn(target: CombatantWithStatus, range: Int, combat: Combat? = null): Pair<Turn, TurnOptionRanking>? {
        val sorted = getTurnOptionRankingList(target, range, combat)
        sorted.forEach {logger.debug { "sorted preferred option: $it" } }
        return if (sorted.isEmpty()) null else sorted[0]
    }

    fun getTurnOptionRankingList(target: CombatantWithStatus, range: Int, combat: Combat? = null)
        : List<Pair<Turn, TurnOptionRanking>>
    {
        val possible = getPossibleTurns(target, range)
        val map = mutableMapOf<Turn, TurnOptionRanking>()

        for (turn in possible) {
            val ranking = TurnOptionRanking.fromTurn(turn)

            if (isValidRanking(target, combat, turn, ranking)) {
                map.put(turn, ranking)
            }
        }

        // TODO: secondary sorting: attacks by DPR, healing by restoration amount needed
        return map.toList().sortedByDescending { it.second.ordinal }
    }

    private fun isValidRanking(
        target: CombatantWithStatus,
        combat: Combat? = null,
        turn: Turn,
        ranking: TurnOptionRanking
    ): Boolean
    {
        val opposingTeam = combat?.getOpponents(this) ?: emptyList()
        val myTeam       = combat?.getMyTeam(this)    ?: listOf(this)

        // println("getPreferredTurn: turn=$turn, ranking=$ranking")
        if (!ranking.isSpell()) {
            return true
        }

        val spell = turn.getSpell()!!
        val ability = spell.getSpellSaveAbility()

        // TODO: remove hard-coding, factor in spell save DC, etc
        if (ability != null && target.getAbilityModifier(ability) > 3) {
            logger.debug { "avoid spell, target has high probability to save: ${spell.name}" }
            return false
        }

        if (spell.isAOE() && opposingTeam.isNotEmpty() && opposingTeam.all { it.isDeadOrDying() }) {
            logger.debug { "avoid AOE spell, all opponents are dying: ${spell.name}" }
            return false
        }

        if (spell.isHealing()) {
            val teamHasACleric = myTeam.any { it.isCleric() && !it.isDeadOrDying() }
            val dyingCount = myTeam.count { it.isDying() }
            val halfHPCount = myTeam.count { it.currentHP <= it.getHP() / 2 }

            if (halfHPCount == 0) {
                logger.debug { "exclude healing spells, no one in party is below half HP" }
                return false
            }

            if (!isCleric() && teamHasACleric && dyingCount <= 1 && halfHPCount <= 3) {
                logger.debug { "exclude healing spells, i'm not a healer and another team mate is" }
                return false
            }
        }

        // TODO: de-prioritize AOE spells if number of targets is low
        // TODO: fireball is AOE, but potentially high damage, so do not exclude

        // TODO: damage resistance -> deprirotize ?

        // exclude spells with damageType that target is immune to
        if (spell.incursDamage()) {
            val damageType = spell.getSpellAttacks(0).first().getDamageList().first().type // TODO: improve
            if (target.getDamageImmunities().contains(damageType)) {
                logger.debug { "skipping spell turn option, target is immune: ${spell.name}" }
                return false
            }
        }
        return true
    }

    fun getActionGoal(combat: Combat? = null): ActionGoal
    {
        if (this.combatant !is PlayerCharacter) {
            return ActionGoal.Attack
        }

        if (! combatant.getPreparedSpells().any { it.isHealing() && isSlotAvailable(it)}) { // if you have no ability to heal ...
            return ActionGoal.Attack
        }

        val myTeam          = combat?.getMyTeam(this)?.filter { !it.isDead() } ?: listOf(this)
        val teamHasACleric  = myTeam.any { it.isCleric() && !it.isDeadOrDying() }
        val dyingCount      = myTeam.count { it.isDying() }
        val halfHPCount     = myTeam.count { it.currentHP <= it.getHP() / 2 }

        if (halfHPCount == 0) {
            logger.debug { "exclude healing spells, no one in party is below half HP" }
            return ActionGoal.Attack
        }

        if (!isCleric() && teamHasACleric && dyingCount <= 1 && halfHPCount <= 3) {
            logger.debug { "exclude healing spells, i'm not a healer and another team member is" }
            return ActionGoal.Attack
        }

        return ActionGoal.Heal
    }

    fun getEffectString(): String {
        val buf = StringBuilder()
        for (effect in toList()) {
            if (buf.isNotEmpty()) {buf.append(",")}
            buf.append(effect)
        }
        return buf.toString()
    }

    fun getConditionString(): String {
        val buf = StringBuilder()
        for (effect in toList()) {
            effect.conditions.forEach {
                if (buf.isNotEmpty()) {buf.append(",")}
                buf.append(it)
            }
        }
        return buf.toString()
    }
}