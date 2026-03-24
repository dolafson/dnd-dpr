package com.vikinghelmet.dnd.dpr.turn

import com.vikinghelmet.dnd.dpr.character.Character
import com.vikinghelmet.dnd.dpr.character.inventory.Weapon
import com.vikinghelmet.dnd.dpr.spells.SpellAttack
import com.vikinghelmet.dnd.dpr.turn.AttackResultField.*
import com.vikinghelmet.dnd.dpr.util.Globals

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
    fun dpr(): Float {
        return damagePerRound.select (getAvgMinMaxSelection())
    }

    fun update(turnId: Int, actionId: Int, effectId: Int, spellAttack: SpellAttack? = null) {
        this.turnId = turnId
        this.actionId = actionId
        this.effectId = effectId
        this.spellAttack = spellAttack
    }

    fun getAvgMinMaxSelection(): AvgMinMaxSelection {
        return if (attackerHadAdvantage == true || targetHadDisadvantageOnSave == true)
            AvgMinMaxSelection.max else AvgMinMaxSelection.avg
    }


    fun getValue(field: AttackResultField): Any {
        // first check for fields that vary for weapon VS spell attack
        if (listOf(weaponDamageDice,weaponDamageBonus,weaponAttackBonus,
                spellSaveAbility,targetSaveBonus).contains(field))
        {
            if (this.attack.action is Weapon) {
                val weapon = this.attack.action as Weapon
                val damageBonus = character.getDamageBonus(weapon, this.attack.isBonusAction ?: false)
                return when (field) {
                    weaponDamageDice -> weapon.damage!!
                    weaponDamageBonus -> damageBonus
                    weaponAttackBonus -> character.getAttackBonus(weapon)
                    spellSaveAbility -> ""
                    targetSaveBonus -> ""
                    else -> {}
                }
            }

            val ability = spellAttack!!.getSaveAbility()
            val bonus = if (ability.isEmpty()) "" else this.attack.monster.properties.getMod(ability)

            return when (field) {
                weaponDamageDice -> ""
                weaponDamageBonus -> ""
                weaponAttackBonus -> ""
                spellSaveAbility -> ability
                targetSaveBonus -> bonus
                else -> {}
            }
        }

        val selection = this.getAvgMinMaxSelection()

        return when (field) {
            level           -> character.getLevel()
            characterName   -> character.getName()
            spellBonusToHit -> character.getSpellBonusToHit()
            spellSaveDC     -> character.getSpellSaveDC()

            monsterName -> this.attack.monster.name
            monsterAC   -> this.attack.monster.properties.dataAcNum

            turn        -> this.turnId
            action      -> if (this.attack.isBonusAction == true) "BA" else ""+this.actionId
            effect      -> this.effectId

            AttackResultField.attack -> if (spellAttack != null) spellAttack.toString() else this.attack.getLabel()

            AttackResultField.startCondition -> Globals.wrapWithQuotes(this.startCondition)
            AttackResultField.numTargets -> this.numTargets

            AttackResultField.chanceToHit -> this.chanceToHit.select(selection)
            AttackResultField.damagePerHit -> this.damagePerHit.select(selection)
            AttackResultField.duration -> this.duration.select(selection)
            fullEffectDamage -> this.damageFullEffect.select(selection)
            else -> {
                println("WARNING: unhandled field: $field")
                Exception("warning").printStackTrace()
            }
        }
    }
}

