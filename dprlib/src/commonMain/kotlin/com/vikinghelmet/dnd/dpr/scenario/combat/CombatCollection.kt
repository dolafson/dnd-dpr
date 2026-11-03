package com.vikinghelmet.dnd.dpr.scenario.combat

import com.vikinghelmet.dnd.dpr.util.Globals
import dev.shivathapaa.logger.api.LoggerFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Transient

class CombatCollection(
    val teamA: List<CombatantWithStatus>,
    val teamB: List<CombatantWithStatus>,
    val numSimulations: Int
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

    fun run() {
        repeat(numSimulations) { runOnce() }
    }

    suspend fun runAsync() {
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
        // each simulation gets its own copies, since Combat mutates HP, location, effects, etc.
        val combat = Combat(battleId++, teamA.map { it.copyForNewCombat() }, teamB.map { it.copyForNewCombat() })
        if (combat.run()) aTeamWinCount++
        combatList.add(combat) // add combat to the list after it is finished running
        return combat
    }

    fun getPercentComplete() = combatList.size.toFloat() / numSimulations.toFloat()

    fun getTeamAWinPercentage(): Float {
        return 100.toFloat() * aTeamWinCount.toFloat() / numSimulations.toFloat()
    }

    fun getTeamADeathPercentage(deathCount: Int): Float {
        var matchCount = 0
        for (combat in combatList) {
            if (deathCount == combat.teamA.count { it.isDead() }) matchCount++
        }
        return 100.toFloat() * matchCount.toFloat() / numSimulations.toFloat()
    }

    fun getTeamADeathPercentages(): String {
        var outputText = ""
        for (x in 0..teamA.size-1) {
            outputText += "\nTeamA ${x} deaths = ${Globals.getPercent(getTeamADeathPercentage(x))}%"
        }
        outputText += "\nTeamA TPK      = ${Globals.getPercent(getTeamADeathPercentage(teamA.size))}%"
        return outputText
    }
}