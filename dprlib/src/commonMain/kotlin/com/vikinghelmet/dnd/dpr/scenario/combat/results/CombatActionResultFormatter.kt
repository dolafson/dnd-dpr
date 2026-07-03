package com.vikinghelmet.dnd.dpr.scenario.combat.results

import com.vikinghelmet.dnd.dpr.scenario.combat.Combat
import com.vikinghelmet.dnd.dpr.scenario.combat.CombatantWithStatus
import com.vikinghelmet.dnd.dpr.scenario.combat.results.CombatActionResultField.entries
import com.vikinghelmet.dnd.dpr.scenario.combat.save.HealthStatus
import com.vikinghelmet.dnd.dpr.util.Globals

object CombatActionResultFormatter {

    fun format(field: CombatActionResultField, combatActionResult: CombatActionResult): String {
        val buf = StringBuilder()
        val value = combatActionResult.getValue(field)
        if (value is Float) {
            buf.append (Globals.getPercent(value))
        }
        else if ("$value".contains(",")) {
            buf.append (Globals.wrapWithQuotes("$value"))
        }
        else {
            buf.append ("$value")
        }
        return buf.append(",").toString()
    }

    fun footer(battleId: Int, currentTurnId: Any, actionLabel: String, teamStatus: List<String>)
        : String
    {
        val buf = StringBuilder()
        entries.forEach {
            val value = when (it) {
                //scenario -> scenarioName
                CombatActionResultField.battle -> battleId
                CombatActionResultField.turn -> currentTurnId
                CombatActionResultField.action -> actionLabel
                CombatActionResultField.actionTaken -> "\"$teamStatus\""
                else -> null
            }
            if (value != null) buf.append(value)
            buf.append(",")
        }
        return buf.toString()
    }

    fun header(): String {
        val buf = StringBuilder("")
        entries.forEach { buf.append(it.name).append(",") }
        return buf.toString()
    }

    fun output(battleId: Int, combatActionResult: CombatActionResult): String {
        val buf = StringBuilder("")
        entries.forEach {
            if (it == CombatActionResultField.battle) {
                buf.append("$battleId,")
            } else {
                buf.append(format(it, combatActionResult))
            }
        }
        return buf.toString()
    }

    fun shortSummary(actionResult: CombatActionResult?): String {
        if (actionResult == null) return ""
        val target = actionResult.target

        // TODO: store location in CombatActionResult ... also, add results just for movement ...
        //val buffer = StringBuilder("(").append(target.shortName()).append(", loc=$location")

        val buffer = StringBuilder("(").append(target.shortName()).append(", ")

        if (actionResult.targetHealth == HealthStatus.positive) {
            buffer.append("hp=${actionResult.targetHP}/${target.getHP()}")
        }
        else {
            buffer.append(actionResult.targetHealth)
        }
        return buffer.append(")").toString()
    }

    fun footer(combat: Combat, turnId: Int, label: String, priorState: MutableMap<CombatantWithStatus, CombatActionResult>, ): String {
        val aList = combat.teamA.map { shortSummary(priorState[it])  }.toList()
        val bList = combat.teamB.map { shortSummary(priorState[it])  }.toList()
        return  footer(combat.battleId, turnId, label, aList) +"\n"+
                footer(combat.battleId, turnId, label, bList) +"\n"
    }

    fun output(combat: Combat): String {
        val priorState = mutableMapOf<CombatantWithStatus, CombatActionResult>() // used during output()
        for (c in (combat.teamA+combat.teamB)) {
            priorState[c] = CombatActionResult(c)
        }

        val buf = StringBuilder()
        buf.append(header()).append("\n")

        for (v in combat.initialState.values.sortedByDescending { it.attacker.initiative }) {
            buf.append (output(combat.battleId,v)).append("\n")
        }

        var outputTurnId = 0

        for (result in combat.actionResultList) {
            if (outputTurnId < result.turnId) {
                buf.append (footer(combat, outputTurnId, "END OF TURN", priorState))
                outputTurnId = result.turnId
            }
            buf.append (output(combat.battleId, result)).append("\n")
            priorState[result.target] = result
        }

        buf.append (footer(combat, outputTurnId, "END OF TURN", priorState))
        return buf.toString()
    }
}