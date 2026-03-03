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
    var targetHadDisadvantageOnSave: Boolean? = false,
    var attackerHadAdvantage: Boolean? = false,
) {
    fun output(character: Character, monster: Monster, attack: Attack, turn: Int, actionId: Int, weapon: Weapon) {
        output(character, monster, attack, turn, actionId,1, weapon,null)
    }

    fun output(character: Character, monster: Monster, attack: Attack,turn: Int, actionId: Int, effect: Int, spellAttack: SpellAttack) {
        output(character, monster, attack, turn, actionId, effect,null, spellAttack)
    }

    private fun output(
        character: Character, monster: Monster, attack: Attack,
        turn: Int, actionId: Int, effect: Int,
        weapon: Weapon?, spellAttack: SpellAttack?
    ) {

        if (weapon == null && spellAttack == null) {
            throw IllegalArgumentException("either weapon or spell attack must be non-null")
        }

        val attackLabel = weapon?.name ?: spellAttack.toString()

        val buf = StringBuilder("")

        if (AttackResultFormatter.isCSV || !AttackResultFormatter.isTxtFirstResultDone) {
            buf.append(AttackResultFormatter.format("level", character.getLevel()))
            buf.append(AttackResultFormatter.format("characterName", character.characterData.name))
            buf.append(AttackResultFormatter.format("spellBonusToHit", character.getSpellBonusToHit()))
            buf.append(AttackResultFormatter.format("spellSaveDC", character.getSpellSaveDC()))

            // TODO: abilities: Str, Dex, ... ?

            buf.append(AttackResultFormatter.format("monsterName", monster.name))
            buf.append(AttackResultFormatter.format("monsterAC", monster.properties.dataAcNum))
            // TODO: abilities: Str, Dex, ... ?

            buf.append(AttackResultFormatter.format("scenario",AttackResultFormatter.scenario))

            if (!AttackResultFormatter.isCSV) {
                println(buf)
                AttackResultFormatter.separate()
                buf.clear()
            }
        }

        buf.append(AttackResultFormatter.format("turn", turn))
        buf.append(AttackResultFormatter.format("action", if (attack.isBonusAction == true) "BA" else ""+actionId))
        buf.append(AttackResultFormatter.format("effect", effect))
        buf.append(AttackResultFormatter.format("attack", attackLabel))

        if (weapon != null) {
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
            val targetSaveBonus = if (spellSaveAbility.isEmpty()) "" else monster.properties.getMod(spellSaveAbility)

            buf.append(AttackResultFormatter.format("spellSaveAbility", spellSaveAbility))
            buf.append(AttackResultFormatter.format("targetSaveBonus", targetSaveBonus))
        }

        buf.append(AttackResultFormatter.format("startCondition", String.format("\"%s\"", EffectManager.toString())))
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
    var scenario = ""

    fun format(fieldName: String, value: Any): String {
        isTxtFirstResultDone = true
        val strValue = if (value is Float) String.format("%2.2f", value) else value

        return if (isCSV) "$strValue," else String.format("\t%-20s %s\n",fieldName, strValue)
    }
    fun formatCSVOnly(fieldName: String, value: Any): String {
        return if (isCSV) format(fieldName, value) else ""
    }

    fun footer(turnId: Any, actionLabel: String, totalDamage: Float) {
        if (isCSV) {
            println(String.format(",,,,,,%s,%s,%s,,,,,,,,,,,,,%2.2f,", AttackResultFormatter.scenario, turnId, actionLabel, totalDamage))
        } else {
            println(format(actionLabel, totalDamage))
        }
    }

    fun separate() { println(txtLineSeparator) }

    fun header() {
        if (!isCSV) { separate(); return }

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
