package com.vikinghelmet.dnd.dpr.scenario.combat

import com.vikinghelmet.dnd.dpr.action.Combatant
import com.vikinghelmet.dnd.dpr.util.Constants
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

    constructor(noStatusTeamA: List<Combatant>, noStatusTeamB: List<Combatant>) : this() {
        val initiativeMap = mutableMapOf<CombatantWithStatus, Int>()
        noStatusTeamA.forEach { add(CombatantWithStatus(it, true), teamA, initiativeMap) }
        noStatusTeamB.forEach { add(CombatantWithStatus(it, false), teamB, initiativeMap) }

        // when computing initiative order, a DM will often use a single roll for an entire group of monsters
        // we won't do that here - unique rolls are more realistic, and easier to implement
        initiativeList = initiativeMap.entries.sortedBy { it.value }.map { it.key }.toList()
    }

    private fun add(combatantWithStatus: CombatantWithStatus,
                    team: MutableList<CombatantWithStatus>,
                    initiativeMap: MutableMap<CombatantWithStatus, Int>)
    {
        team.add(combatantWithStatus)
        initiativeMap[combatantWithStatus] = (1..20).random() + combatantWithStatus.getInitiativeBonus()
    }

    fun run() {
        while (!teamA.all { it.isDead() } && !teamB.all { it.isDead() }) {
            fullTurn()
            turn++
        }
    }

    fun fullTurn() {
        initiativeList.forEach { combatant ->
            if (combatant.isDead()) {
                logger.debug { "combatant is dead: $combatant"}
            }
            else if (combatant.isDying()) {
                // TODO: roll for death saving throw
                logger.debug { "death saving throw: $combatant"}
            }
            else if (!combatant.canTakeAction()) {
                logger.debug { "combatant can not take action: $combatant"}
            }
            else {
                takeTurn(combatant)
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
            runToward(combatant, closest)  // TODO: improve target selection
            return closest
        }

        // if you are too close for comfort ... run away before picking a target
        if (closestDistance <= preferredDistance) {
            runAway (combatant, targetList, closestDistance)
        }

        return targetList.minByOrNull { it.distance(combatant.location) }!! // TODO: improve target selection
    }

    fun runAway(combatant: CombatantWithStatus, targetList: MutableList<CombatantWithStatus>, closestDistanceStart: Double) {
        var closestDistance = closestDistanceStart
        val maxMoves = combatant.getWalkingSpeed() / 5
        var loc = combatant.location
        val targetLocationList = targetList.map { it.location }.toList()

        for (i in 1..maxMoves) {
            for (oneOffLoc in loc.getOneOff()) {
                // for the given new location, find the closest target
                val nextClosest = targetLocationList.minByOrNull { it.distance(oneOffLoc) }
                val nextClosestDistance = nextClosest!!.distance(oneOffLoc)

                // if the new "closest" is larger than before, that's progress: take it
                if (closestDistance < nextClosestDistance) {
                    closestDistance = nextClosestDistance
                    loc = oneOffLoc
                    break
                }
            }
            if (combatant.location == loc) break
            combatant.location = loc
        }
    }

    fun runToward(combatant: CombatantWithStatus, target: CombatantWithStatus) {
        val maxMoves = combatant.getWalkingSpeed() / 5
        val tloc = target.location
        for (i in 1..maxMoves) {
            if (combatant.location.x < tloc.x -1) combatant.location.x++
            else if (combatant.location.x > tloc.x +1) combatant.location.x--
            else if (combatant.location.y < tloc.y -1) combatant.location.y++
            else if (combatant.location.y > tloc.y +1) combatant.location.y--
            else break
        }
    }

    fun takeTurn(combatant: CombatantWithStatus) {
        val target = chooseTarget(combatant)

        // choose target and action
        // if not within attack range, move
        // if successful, modify target status
        // if not within desired post-attack range, move
    }
}
