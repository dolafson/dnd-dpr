package com.vikinghelmet.dnd.dpr.turn

import com.vikinghelmet.dnd.dpr.character.Character
import com.vikinghelmet.dnd.dpr.character.inventory.Weapon
import com.vikinghelmet.dnd.dpr.spells.SpellAttack

data class AttackResult(
    val numTargets: Int,
    val chanceToHit: AvgMinMax,
    val damagePerHit: AvgMinMax,
    val damagePerRound: AvgMinMax,
    val duration: AvgMinMax,
    val damageFullEffect: AvgMinMax, // for entire duration of spell, and/or sum across multiple targets

    var character: Character,
    var attack: Attack,
    var startCondition: String,

    var targetHadDisadvantageOnSave: Boolean? = false,
    var attackerHadAdvantage: Boolean? = false,

    // fields that get updated via post-processing ...
    var turnId: Int = -1,
    var actionId: Int = -1,
    var effectId: Int = -1,
    var spellAttack: SpellAttack? = null,
) {
    fun update(turnId: Int, actionId: Int, effectId: Int, spellAttack: SpellAttack? = null) {
        this.turnId = turnId
        this.actionId = actionId
        this.effectId = effectId
        this.spellAttack = spellAttack
    }

    fun output(scenarioName: String) {
        val attackLabel = if (spellAttack != null) spellAttack.toString() else attack.getLabel()
        val buf = StringBuilder("")

        if (AttackResultFormatter.isCSV || !AttackResultFormatter.isTxtFirstResultDone) {
            buf.append(AttackResultFormatter.format("level", character.getLevel()))
            buf.append(AttackResultFormatter.format("characterName", character.characterData.name))
            buf.append(AttackResultFormatter.format("spellBonusToHit", character.getSpellBonusToHit()))
            buf.append(AttackResultFormatter.format("spellSaveDC", character.getSpellSaveDC()))

            // TODO: abilities: Str, Dex, ... ?

            buf.append(AttackResultFormatter.format("monsterName", attack.monster.name))
            buf.append(AttackResultFormatter.format("monsterAC", attack.monster.properties.dataAcNum))
            // TODO: abilities: Str, Dex, ... ?

            if (!AttackResultFormatter.isCSV) {
                println(buf)
                AttackResultFormatter.separate()
                buf.clear()
            }
        }

        buf.append(AttackResultFormatter.format("scenario",scenarioName))

        buf.append(AttackResultFormatter.format("turn", turnId))
        buf.append(AttackResultFormatter.format("action", if (attack.isBonusAction == true) "BA" else ""+actionId))
        buf.append(AttackResultFormatter.format("effect", effectId))
        buf.append(AttackResultFormatter.format("attack", attackLabel))

        if (attack.action is Weapon) {
            val weapon = attack.action as Weapon
            val damageBonus = character.getDamageBonus(weapon, attack.isBonusAction?:false)
            buf.append(AttackResultFormatter.format("weaponDamage", weapon.damage!!))
            buf.append(AttackResultFormatter.format("weaponDamageBonus", damageBonus))
            buf.append(AttackResultFormatter.format("weaponAttackBonus", character.getAttackBonus(weapon)))

            // for weapons, if fmt=txt, do not dump save data
            buf.append(AttackResultFormatter.formatCSVOnly("spellSaveAbility", ""))
            buf.append(AttackResultFormatter.formatCSVOnly("targetSaveBonus", ""))
        } else {
            // for spells, if fmt=txt, do not dump weapon data
            buf.append(AttackResultFormatter.formatCSVOnly("weaponDamageDice", ""))
            buf.append(AttackResultFormatter.formatCSVOnly("weaponDamageBonus", ""))
            buf.append(AttackResultFormatter.formatCSVOnly("weaponAttackBonus", ""))

            val spellSaveAbility = spellAttack!!.getSaveAbility()
            val targetSaveBonus = if (spellSaveAbility.isEmpty()) "" else attack.monster.properties.getMod(spellSaveAbility)

            buf.append(AttackResultFormatter.format("spellSaveAbility", spellSaveAbility))
            buf.append(AttackResultFormatter.format("targetSaveBonus", targetSaveBonus))
        }

        buf.append(AttackResultFormatter.format("startCondition", String.format("\"%s\"", startCondition)))
        buf.append(AttackResultFormatter.format("numTargets", numTargets))

        val selection = getAvgMinMaxSelection()

        buf.append(AttackResultFormatter.format("chanceToHit", chanceToHit.select(selection)))
        buf.append(AttackResultFormatter.format("damagePerHit", damagePerHit.select(selection)))
        buf.append(AttackResultFormatter.format("duration", duration.select(selection)))
        buf.append(AttackResultFormatter.format("damageFullEffect", damageFullEffect.select(selection)))

        println(buf)
    }

    fun getAvgMinMaxSelection(): AvgMinMaxSelection {
        return if (attackerHadAdvantage == true || targetHadDisadvantageOnSave == true)
            AvgMinMaxSelection.max else AvgMinMaxSelection.avg
    }
}

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
