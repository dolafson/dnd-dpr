package com.vikinghelmet.dnd.dpr.scenario

import com.vikinghelmet.dnd.dpr.character.Character
import com.vikinghelmet.dnd.dpr.turn.AttackResult
import com.vikinghelmet.dnd.dpr.turn.AttackResultFormatter
import dev.shivathapaa.logger.api.LoggerFactory
import kotlinx.serialization.Transient

data class ScenarioResult(
    val scenario: Scenario,
    val attackResults: List<AttackResult>,
    val totalDPR: Float = 0f,
) {
    @Transient private val logger = LoggerFactory.get(Character::class.simpleName ?: "")

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is ScenarioResult) return false
        //return (scenario == other.scenario && totalDPR == other.totalDPR)
        return (totalDPR == other.totalDPR)
    }

    fun dprAtRound(round: Int):Float {
        return attackResults.firstOrNull { it.turnId == round }?.dpr() ?: 0f
    }

    fun output(): String {
        val scenarioName = scenario.getLabel()
        val firstResult = attackResults.firstOrNull { it.turnId == 1 }
        if (firstResult == null) {
            logger.warn { "No result for $scenarioName" } // this is often true for defensive spells, but worth checking
            return ""
        }

        val buf = StringBuilder()
        buf.append(AttackResultFormatter.header(scenarioName, firstResult)).append("\n")

        for (turnId in 1..scenario.turns.size) {
            var turnDPR = 0f
            for (result in attackResults) if (result.turnId == turnId) {
                buf.append (AttackResultFormatter.output(result)).append("\n")
                turnDPR += result.dpr()
            }

            buf.append(AttackResultFormatter.footer(turnId, "TURN TOTAL", turnDPR)).append("\n")
        }

        buf.append(AttackResultFormatter.footer("", "SCENARIO TOTAL", totalDPR)).append("\n")
        return buf.toString()
    }

    companion object {
        fun topResults(resultList: List<ScenarioResult>, max: Int): List<ScenarioResult> {
            // first prioritize totalDPR, and if multiple scenarios have the same total, sort them by highest first round damage
            if (resultList.size == 0) return resultList

            // NOTE: THERE IS NO ROUND ZERO; START AT ONE
            return resultList.sortedWith(
                compareByDescending<ScenarioResult> { it.totalDPR } .thenByDescending { it.dprAtRound(1) }
            ).distinct().take(kotlin.math.min(max,resultList.size))
        }
    }

    override fun hashCode(): Int {
        var result = totalDPR.hashCode()
//        result = 31 * result + scenario.hashCode()
//        result = 31 * result + attackResults.hashCode()
        return result
    }
}