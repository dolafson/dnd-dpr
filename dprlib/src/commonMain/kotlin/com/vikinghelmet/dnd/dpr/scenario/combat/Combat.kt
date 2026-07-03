package com.vikinghelmet.dnd.dpr.scenario.combat

import com.vikinghelmet.dnd.dpr.action.Combatant
import com.vikinghelmet.dnd.dpr.monsters.Monster
import com.vikinghelmet.dnd.dpr.scenario.combat.results.CombatActionResult
import dev.shivathapaa.logger.api.LoggerFactory
import kotlinx.serialization.Transient
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.fetchAndIncrement

@OptIn(ExperimentalAtomicApi::class)
class Combat(val battleId: Int) {
    @Transient
    private val logger = LoggerFactory.get(Combat::class.simpleName ?: "")
    val teamA = mutableListOf<CombatantWithStatus>()
    val teamB = mutableListOf<CombatantWithStatus>()
    var initiativeList = listOf<CombatantWithStatus>()
    val actionResultList = mutableListOf<CombatActionResult>()
    var turnId = 0

    val initialState = mutableMapOf<CombatantWithStatus, CombatActionResult>() // used for debugging

    private fun pad2(input: Int) = input.toString().padStart(2, '0')
    private fun getAttrString() = mapOf("battleId" to pad2(battleId), "turnId" to pad2(turnId)).toString()
    fun logError(msg: () -> String) = logger.error { getAttrString() +": "+ msg() }
    fun logWarn(msg: () -> String)  = logger.warn  { getAttrString() +": "+ msg() }
    fun logInfo(msg: () -> String)  = logger.info  { getAttrString() +": "+ msg() }
    fun logDebug(msg: () -> String) = logger.debug { getAttrString() +": "+ msg() }

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

    fun isRunning() =  (teamA.any { it.isPositive() } && teamB.any { it.isPositive() })

    fun run(): Boolean {
        logInfo { "initiativeList = ${ initiativeList.associateBy { it.initiative }}" }

        while (isRunning()) {
            logInfo { "teamA: ${ teamSummary(teamA) }, teamB: ${ teamSummary(teamB) }" }

            fullTurn()
            turnId++
            // exit after 100 turns, or in any aberrant situation where a live A and B occupy the same space
            val notDeadA = teamA.filter { it.isPositive() }
            val notDeadB = teamB.filter { it.isPositive() }
            val runningTooLong = turnId > 100

            while (notDeadA.any { it -> notDeadB.any { it2 -> it2.location == it.location }}) {
                notDeadA.filter { it -> notDeadB.any { it2 -> it2.location == it.location }}.forEach {
                    logError { "combatant in same space as opponents; jitter: $it" }
                    it.location.jitter()
                }
            }

            if (runningTooLong) {
                repeat(3) { logWarn { "" } }
                logWarn {"combat is running too long" }

                logWarn {"teamA = ${ teamSummary(teamA)}" }
                logWarn {"teamB = ${ teamSummary(teamB)}" }

                // throw IllegalStateException("turnId=$turnId, combat is running too long")
                return false
            }
        }
        if (!teamB.any { it.isPositive() }) {
            logWarn { "winner = teamA = ${ teamSummary(teamA) } " }
            return true
        } else {
            logWarn { "winner = teamB = ${ teamSummary(teamB) } " }
            return false
        }
    }

    fun teamSummary(team: List<CombatantWithStatus>): String {
        return "${ team.sortedByDescending { it.initiative }.map { it.summary() }.toList() }"
    }

    fun fullTurn() {
        if (initiativeList.isEmpty()) {
            logWarn { "initiative list is empty!!" }
            return
        }

        for (combatant in initiativeList)
        {
            if (!isRunning()) { // combat may end mid-turn, once either team has no remaining members with HP > 0
                break
            }

            actionResultList.addAll (CombatTurn(this, combatant).fullTurn())
        }
    }

    fun getOpponents(combatant: CombatantWithStatus) = if (combatant.onTeamA) teamB else teamA
    fun getMyTeam(combatant: CombatantWithStatus) = if (combatant.onTeamA) teamA else teamB
}
