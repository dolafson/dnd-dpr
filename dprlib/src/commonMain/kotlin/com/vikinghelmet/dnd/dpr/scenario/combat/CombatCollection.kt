package com.vikinghelmet.dnd.dpr.scenario.combat

import com.vikinghelmet.dnd.dpr.action.Combatant
import dev.shivathapaa.logger.api.LoggerFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Transient

class CombatCollection(
    val teamA: List<Combatant>,
    val teamB: List<Combatant>,
    val numSimulations: Int,
    val flightSupported: Boolean = false
)
{
    @Transient
    private val logger = LoggerFactory.get(CombatCollection::class.simpleName ?: "")

    var combatList = mutableListOf<Combat>()
    var aTeamWinCount = 0
    var battleId = 0

    fun log() {
        logger.warn { "teamA: $teamA" }
        logger.warn { "teamB: $teamB" }
    }

    suspend fun run() { // async, avg 4.7s
        /*
        coroutineScope {
            repeat(numSimulations) { this.launch { runOnce() } }
        }
         */
        withContext(Dispatchers.Default) {
            repeat(numSimulations) { this.launch { runOnce() } }
        }
    }

    fun runOnce(): Combat {
        val combat = Combat(battleId++, teamA, teamB, flightSupported = flightSupported)
        if (combat.run()) aTeamWinCount++
        combatList.add(combat) // add combat to the list after it is finished running
        return combat
    }

    fun getPercentComplete() = combatList.size.toFloat() / numSimulations.toFloat()

    fun getTeamAWinPercentage(): Float {
        return 100.toFloat() * aTeamWinCount.toFloat() / numSimulations.toFloat()
    }
}