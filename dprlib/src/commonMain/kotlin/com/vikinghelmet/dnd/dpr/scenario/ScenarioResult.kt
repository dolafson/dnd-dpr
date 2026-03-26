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
}