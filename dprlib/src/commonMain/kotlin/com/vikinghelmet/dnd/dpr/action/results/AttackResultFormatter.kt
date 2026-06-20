package com.vikinghelmet.dnd.dpr.action.results

import com.vikinghelmet.dnd.dpr.action.results.AttackResultField.*
import com.vikinghelmet.dnd.dpr.util.Globals

object AttackResultFormatter {
    val txtLineSeparator = "#######################################################\n"
    var isCSV: Boolean = false
    var scenarioName = ""

    fun format(field: AttackResultField, attackResult: AttackResult): String {
        //return format(field.name, if (field == scenario) scenarioName else attackResult.getValue(field))
        return format(field.name, attackResult.getValue(field))
    }
    
    fun format(fieldName: String, value: Any): String {
        val strValue = if (value is Float) Globals.getPercent(value) else value.toString()

        if (isCSV && strValue.contains(",")) {  // wrap in double-quotes only if CSV and value has a comma
            return "\"$strValue\","
        }
        return if (isCSV) "$strValue," else "\t${ Globals.rightPad(fieldName,20) }${strValue}\n"
    }

    fun footer(currentTurnId: Any, actionLabel: String, totalDamage: Float): String {
        if (!isCSV) {
            return format(actionLabel, totalDamage)
        }

        val buf = StringBuilder()
        entries.forEach {
            val value = when (it) {
                //scenario -> scenarioName
                turn -> currentTurnId
                action -> actionLabel
                fullEffectDamage -> Globals.getPercent(totalDamage)
                else -> null
            }
            if (value != null) buf.append(value)
            buf.append(",")
        }
        return buf.toString()
    }

    fun separate() { println(txtLineSeparator) }

    fun header(scenarioName: String, firstAttackResult: AttackResult): String {
        this.scenarioName = scenarioName
        val buf = StringBuilder("")
        if (isCSV) {
            entries.forEach { buf.append(it.name).append(",") }
            return buf.toString()
        }

        // for plain text output, display a block of turn-invariant fields first
        // no need to display them on every turn
        separate();

        entries.filter { it.constantAcrossTurns }.forEach {
            buf.append(format(it, firstAttackResult))
        }
        return buf.toString()
    }

    fun output(attackResult: AttackResult): String {
        val buf = StringBuilder("")

        entries.filter { isCSV || ! it.constantAcrossTurns }.forEach {
            buf.append(format(it, attackResult))
        }
        return buf.toString()
    }

}