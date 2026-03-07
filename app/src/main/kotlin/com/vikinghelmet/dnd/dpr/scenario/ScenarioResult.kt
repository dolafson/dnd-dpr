package com.vikinghelmet.dnd.dpr.scenario

import com.vikinghelmet.dnd.dpr.turn.AttackResult
import com.vikinghelmet.dnd.dpr.turn.AttackResultFormatter

data class ScenarioResult(
    val scenario: Scenario,
    val attackResults: List<AttackResult>,
    val totalDPR: Float = 0f,
) {
    fun output() {
        var turnId = 1
        val scenarioName = scenario.getLabel()
        AttackResultFormatter.header(scenarioName)

        for (turn in scenario.turns) {
            var turnDPR = 0f

            for (result in attackResults) if (result.turnId == turnId) {
                result.output(scenarioName)
                turnDPR += result.damagePerRound.select (result.getAvgMinMaxSelection())
            }

            AttackResultFormatter.footer(turnId++, "TURN TOTAL", turnDPR, scenarioName)
        }

        AttackResultFormatter.footer("", "SCENARIO TOTAL", totalDPR, scenarioName)
    }
}