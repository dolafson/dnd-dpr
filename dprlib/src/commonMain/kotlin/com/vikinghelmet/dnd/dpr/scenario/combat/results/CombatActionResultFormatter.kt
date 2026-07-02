package com.vikinghelmet.dnd.dpr.scenario.combat.results

import com.vikinghelmet.dnd.dpr.scenario.combat.results.CombatActionResultField.entries
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
}