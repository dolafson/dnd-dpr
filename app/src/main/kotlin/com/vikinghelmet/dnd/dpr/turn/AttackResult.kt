package com.vikinghelmet.dnd.dpr.turn

import com.vikinghelmet.dnd.dpr.character.Character
import com.vikinghelmet.dnd.dpr.character.inventory.Weapon
import com.vikinghelmet.dnd.dpr.monsters.Monster
import com.vikinghelmet.dnd.dpr.spells.SpellAttack

data class AttackResult(
    val numTargets: Int,
    val chanceToHit: AvgMinMax,
    val damagePerHit: AvgMinMax,
    val damagePerRound: AvgMinMax,
    val duration: AvgMinMax,
    val damageFullEffect: AvgMinMax, // for entire duration of spell, and/or sum across multiple targets
) {
    fun output(
        format: String, character: Character, monster: Monster, attack: Attack,
        scenario: String, turn: Int, action: String, effect: Int, weapon: Weapon?, spellAttack: SpellAttack?
    ) {

        if (weapon == null && spellAttack == null) {
            throw IllegalArgumentException("either weapon or spell attack must be non-null")
        }

        val attackLabel = weapon?.name ?: spellAttack.toString()

        val buf = StringBuilder("")

        if (format == "csv" || !AttackResultFormatter.isTxtFirstResultDone) {
            buf.append(AttackResultFormatter.format(format, "level", character.getLevel()))
            buf.append(AttackResultFormatter.format(format, "characterName", character.characterData.name))
            buf.append(AttackResultFormatter.format(format, "spellBonusToHit", character.getSpellBonusToHit()))
            buf.append(AttackResultFormatter.format(format, "spellSaveDC", character.getSpellSaveDC()))

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

        buf.append(AttackResultFormatter.format(format, "turn", turn))
        buf.append(AttackResultFormatter.format(format, "action", action))
        buf.append(AttackResultFormatter.format(format, "effect", effect))
        buf.append(AttackResultFormatter.format(format, "attack", attackLabel))

        if (weapon != null) {
            val damageBonus = character.getDamageBonus(weapon, attack.isBonusAction?:false)
            buf.append(AttackResultFormatter.format(format, "weaponDamage", weapon.damage!!))
            buf.append(AttackResultFormatter.format(format, "weaponDamageBonus", damageBonus))
            buf.append(AttackResultFormatter.format(format, "weaponAttackBonus", character.getAttackBonus(weapon)))

            // for weapons, if fmt=txt, do not dump save data
            buf.append(AttackResultFormatter.formatCSVOnly(format, "spellSaveAbility", ""))
            buf.append(AttackResultFormatter.formatCSVOnly(format, "targetSaveBonus", ""))
        } else {
            // for spells, if fmt=txt, do not dump weapon data
            buf.append(AttackResultFormatter.formatCSVOnly(format, "weaponDamageDice", ""))
            buf.append(AttackResultFormatter.formatCSVOnly(format, "weaponDamageBonus", ""))
            buf.append(AttackResultFormatter.formatCSVOnly(format, "weaponAttackBonus", ""))

            val spellSaveAbility = spellAttack!!.getSaveAbility()
            val targetSaveBonus = if (spellSaveAbility.isEmpty()) "" else monster.properties.getMod(spellSaveAbility)

            buf.append(AttackResultFormatter.format(format, "spellSaveAbility", spellSaveAbility))
            buf.append(AttackResultFormatter.format(format, "targetSaveBonus", targetSaveBonus))
        }

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
        val strValue = if (value is Float) String.format("%2.2f", value) else value

        return if (fmt == "csv") "$strValue,"
        else String.format("\t%-20s %s\n",fieldName, strValue)
    }
    fun formatCSVOnly(fmt: String, fieldName: String, value: Any): String {
        return if (fmt != "csv") "" else format(fmt,fieldName,value)
    }

    fun footer(outputFormat: String, scenario: String, turnId: Any, actionLabel: String, totalDamage: Float) {
        if (outputFormat != "csv") {
            println(format(outputFormat, actionLabel, totalDamage))
            return
        }
        println(String.format(",,,,,,%s,%s,%s,,,,,,,,,,,,%2.2f,", scenario, turnId, actionLabel, totalDamage))
    }

    fun separate() { println(txtLineSeparator) }

    fun header(fmt: String) {
        if (fmt != "csv") { separate(); return }

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

        buf.append("numTargets").append(",")
        // TODO: endCondition ?

        buf.append("chanceToHit").append(",")
        buf.append("damagePerHit").append(",")
        buf.append("duration").append(",")
        buf.append("fullEffectDamage").append(",")
        println(buf)
    }
}
