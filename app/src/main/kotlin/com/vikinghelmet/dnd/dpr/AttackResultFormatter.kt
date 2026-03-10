package com.vikinghelmet.dnd.dpr

object AttackResultFormatter {
    val txtLineSeparator = "#######################################################\n"
    var isTxtFirstResultDone = false
    var isCSV: Boolean = false

    fun format(fieldName: String, value: Any): String {
        isTxtFirstResultDone = true
        val strValue = if (value is Float) String.format("%2.2f", value) else value

        return if (isCSV) "$strValue," else String.format("\t%-20s %s\n",fieldName, strValue)
    }
    fun formatCSVOnly(fieldName: String, value: Any): String {
        return if (isCSV) format(fieldName, value) else ""
    }

    fun footer(turnId: Any, actionLabel: String, totalDamage: Float, scenarioName: String) {
        if (isCSV) {
            println(String.format(",,,,,,%s,%s,%s,,,,,,,,,,,,,%2.2f,", scenarioName, turnId, actionLabel, totalDamage))
        } else {
            println(format(actionLabel, totalDamage))
        }
    }

    fun separate() { println(txtLineSeparator) }

    fun header(scenarioName: String) {
        if (!isCSV) {
            separate();
            //println(format("scenario",scenarioName))
            //println(String.format("\t%-20s %s\n","scenario",scenarioName))
            //println()
            return
        }

        val buf = StringBuilder("")

        buf.append("level").append(",")
        buf.append("characterName").append(",")
        buf.append("spellBonusToHit").append(",")
        buf.append("spellSaveDC").append(",")

        // TODO: abilities: Str, Dex, ... ?

        buf.append("monsterName").append(",")
        buf.append("monsterAC").append(",")
        // TODO: abilities: Str, Dex, ... ?

        buf.append("scenario").append(",")
        buf.append("turn").append(",")
        buf.append("action").append(",")
        buf.append("effect").append(",")
        buf.append("attack").append(",")

        buf.append("weaponDamageDice").append(",")
        buf.append("weaponDamageBonus").append(",")
        buf.append("weaponAttackBonus").append(",")

        buf.append("spellSaveAbility").append(",")
        buf.append("targetSaveBonus").append(",")

        buf.append("startCondition").append(",")
        buf.append("numTargets").append(",")

        buf.append("chanceToHit").append(",")
        buf.append("damagePerHit").append(",")
        buf.append("duration").append(",")
        buf.append("fullEffectDamage").append(",")
        println(buf)
    }
}