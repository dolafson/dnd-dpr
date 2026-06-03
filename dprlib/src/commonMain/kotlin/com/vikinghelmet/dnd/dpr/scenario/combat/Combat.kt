package com.vikinghelmet.dnd.dpr.scenario.combat

import com.vikinghelmet.dnd.dpr.action.Combatant
import dev.shivathapaa.logger.api.LoggerFactory
import kotlinx.serialization.Transient

class Combat()
{
    @Transient private val logger = LoggerFactory.get(Combat::class.simpleName ?: "")
    val teamA = mutableListOf<CombatantWithStatus>()
    val teamB = mutableListOf<CombatantWithStatus>()
    var initiativeList = listOf<CombatantWithStatus>()
    val combatActionList = mutableListOf<CombatAction>()

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

    fun takeTurn(combatant: CombatantWithStatus) {
        // if you are within 5 feet of an enemy, movement is not an option for now
        // (it would provoke an attack of oppy, and the value threshold for that
        // is hard to compute)
        val targetList = if (combatant.onTeamA) teamB else teamA
        // val inMeleeRange = targetList.filter {  }

        // choose target and action
        // if not within attack range, move
        // if successful, modify target status
        // if not within desired post-attack range, move
    }
}
