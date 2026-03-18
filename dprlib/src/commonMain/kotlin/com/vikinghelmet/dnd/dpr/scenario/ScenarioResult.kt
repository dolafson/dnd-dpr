package com.vikinghelmet.dnd.dpr.scenario

import com.vikinghelmet.dnd.dpr.turn.AttackResult
import com.vikinghelmet.dnd.dpr.turn.AttackResultFormatter

data class ScenarioResult(
    val scenario: Scenario,
    val attackResults: List<AttackResult>,
    val totalDPR: Float = 0f,
) {
    fun output(): String {
        var turnId = 1
        val scenarioName = scenario.getLabel()
        val buf = StringBuilder()
        buf.append(AttackResultFormatter.header(scenarioName)).append("\n")

        for (turn in scenario.turns) {
            var turnDPR = 0f

            for (result in attackResults) if (result.turnId == turnId) {
                buf.append (result.output(scenarioName)).append("\n")
                turnDPR += result.damagePerRound.select (result.getAvgMinMaxSelection())
            }

            buf.append(AttackResultFormatter.footer(turnId++, "TURN TOTAL", turnDPR, scenarioName)).append("\n")
        }

        buf.append(AttackResultFormatter.footer("", "SCENARIO TOTAL", totalDPR, scenarioName)).append("\n")
        return buf.toString()
    }
}