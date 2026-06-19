package com.vikinghelmet.dnd.dpr.scenario.combat

import com.vikinghelmet.dnd.dpr.action.Combatant
import dev.shivathapaa.logger.api.LoggerFactory
import kotlinx.serialization.Transient

class CombatLoop(
    val teamA: List<Combatant>,
    val teamB: List<Combatant>,
    val numSimulations: Int,
    val flightSupported: Boolean = false
)
{
    @Transient
    private val logger = LoggerFactory.get(CombatLoop::class.simpleName ?: "")

    var aTeamWinCount = 0
    var battleId = 0

    fun log() {
        logger.warn { "teamA: $teamA" }
        logger.warn { "teamB: $teamB" }
    }
    // run the specified number of simulations, then return teamA win percentage
    fun run() = repeat(numSimulations) { runOnce() }

    fun runOnce() {
        if (Combat(battleId++, teamA, teamB, flightSupported = flightSupported).run()) aTeamWinCount++
    }

    fun getPercentComplete() = battleId.toFloat() / numSimulations.toFloat()

    fun getTeamAWinPercentage(): Float {
        return aTeamWinCount.toFloat() / battleId.toFloat()
    }
}