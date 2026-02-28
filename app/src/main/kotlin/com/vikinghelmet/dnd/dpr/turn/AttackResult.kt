package com.vikinghelmet.dnd.dpr.turn

import com.vikinghelmet.dnd.dpr.character.Character
import com.vikinghelmet.dnd.dpr.monsters.Monster

data class AttackResult(
    val chanceToHit: AvgMinMax,
    val damagePerHit: AvgMinMax,
    val damagePerRound: AvgMinMax,
    val duration: AvgMinMax,
    val damageFullEffect: AvgMinMax, // for entire duration of spell, and/or sum across multiple targets
) {
    fun output(format: String, character: Character, monster: Monster, scenario: String,
               turn: Int, action: String, effect: Int, attackLabel: String, numTargets: Int)
    {
        val buf = StringBuilder("")

        if (format == "csv" || !AttackResultFormatter.isTxtFirstResultDone) {
            buf.append(AttackResultFormatter.format(format, "level", character.getLevel()))

            buf.append(AttackResultFormatter.format(format, "characterName", character.characterData.name))
            buf.append(AttackResultFormatter.format(format, "spellBonusToHit", character.getSpellBonusToHit()))
            // TODO: abilities: Str, Dex, ... ?

            buf.append(AttackResultFormatter.format(format, "monsterName", monster.name))
            buf.append(AttackResultFormatter.format(format, "monsterAC", monster.properties.dataAcNum))
            // TODO: abilities: Str, Dex, ... ?

            buf.append(AttackResultFormatter.format(format, "scenario", scenario))

            if (format != "csv") {
                println(buf)
                AttackResultFormatter.separate()
                buf.clear()
            }
        }

        buf.append(AttackResultFormatter.format(format,"turn", turn))
        buf.append(AttackResultFormatter.format(format,"action", action))
        buf.append(AttackResultFormatter.format(format,"effect", effect))
        buf.append(AttackResultFormatter.format(format,"attack", attackLabel))
        buf.append(AttackResultFormatter.format(format,"numTargets", numTargets))
        // TODO: endCondition ?

        buf.append(AttackResultFormatter.format(format,"chanceToHit", chanceToHit.avg))
        buf.append(AttackResultFormatter.format(format,"damagePerHit", damagePerHit.avg))
        buf.append(AttackResultFormatter.format(format,"duration", duration.avg))
        buf.append(AttackResultFormatter.format(format,"damageFullEffect", damageFullEffect.avg))
        println(buf)
    }
}

object AttackResultFormatter {
    val txtLineSeparator = "#######################################################\n"

    var isTxtFirstResultDone = false

    fun format(fmt: String, fieldName: String, value: Any): String {
        isTxtFirstResultDone = true
        return if (fmt == "csv") "$value,"
        else String.format("\t%-20s %s\n",fieldName, value)
    }

    fun footer(outputFormat: String, scenario: String, turnId: Any, actionLabel: String, totalDamage: Float) {
        if (outputFormat != "csv") {
            println(format(outputFormat, actionLabel, totalDamage))
            return
        }
        println(String.format(",,,,,%s,%s,%s,,,,,,,%2.2f,", scenario, turnId, actionLabel, totalDamage))
    }

    fun separate() { println(txtLineSeparator) }

    fun header(fmt: String) {
        if (fmt != "csv") { separate(); return }

        val buf = StringBuilder("")

        buf.append("level").append(",")
        buf.append("characterName").append(",")
        buf.append("spellBonusToHit").append(",")
        // TODO: abilities: Str, Dex, ... ?

        buf.append("monsterName").append(",")
        buf.append("monsterAC").append(",")
        // TODO: abilities: Str, Dex, ... ?

        buf.append("scenario").append(",")
        buf.append("turn").append(",")
        buf.append("action").append(",")
        buf.append("effect").append(",")
        buf.append("attack").append(",")
        buf.append("numTargets").append(",")
        // TODO: endCondition ?

        buf.append("chanceToHit").append(",")
        buf.append("damagePerHit").append(",")
        buf.append("duration").append(",")
        buf.append("damagePerDuration").append(",")
        println(buf)
    }
}
