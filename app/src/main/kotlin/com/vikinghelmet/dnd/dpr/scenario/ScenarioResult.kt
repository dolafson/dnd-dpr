package com.vikinghelmet.dnd.dpr.scenario

import com.vikinghelmet.dnd.dpr.turn.AttackResult
import com.vikinghelmet.dnd.dpr.turn.AttackResultFormatter

data class ScenarioResult(
    val attackResults: List<AttackResult>,
    val totalDPR: Float = 0f
) {
    fun output(scenario: Scenario) {
        var turnId = 1

        for (turn in scenario.turns) {
            var turnDPR: Float = 0f
            AttackResultFormatter.header()

            for (result in attackResults) if (result.turnId == turnId) {
                result.output()
                turnDPR += result.damagePerRound.select (result.getAvgMinMaxSelection())
            }

            AttackResultFormatter.footer(turnId++, "TURN TOTAL", turnDPR)
        }

        AttackResultFormatter.footer("", "SCENARIO TOTAL", totalDPR)
    }
}