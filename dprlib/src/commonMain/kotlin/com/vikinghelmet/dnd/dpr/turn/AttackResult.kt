package com.vikinghelmet.dnd.dpr.turn

import com.vikinghelmet.dnd.dpr.character.Character
import com.vikinghelmet.dnd.dpr.character.inventory.Weapon
import com.vikinghelmet.dnd.dpr.spells.SpellAttack
import com.vikinghelmet.dnd.dpr.turn.AttackResultField.*
import com.vikinghelmet.dnd.dpr.util.Globals
import com.vikinghelmet.dnd.dpr.util.Globals.probableResult
import com.vikinghelmet.dnd.dpr.util.Globals.toAvg
import dev.shivathapaa.logger.api.LoggerFactory
import kotlinx.serialization.Transient

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

    // fields that get updated via post-processing ...
    var turnId: Int = -1,
    var actionId: Int = -1,
    var effectId: Int = -1,
    var spellAttack: SpellAttack? = null,
    var avgMinMaxSelection:AvgMinMaxSelection = AvgMinMaxSelection.avg
) {
    @Transient private val logger = LoggerFactory.get(AttackResult::class.simpleName ?: "")

    override fun toString(): String = "($turnId,$actionId,$effectId): chanceToHit=$chanceToHit, damagePerRound=$damagePerRound, selection=$avgMinMaxSelection"

    fun merge(secondary: AttackResult, chanceOfSuccess: Float): AttackResult
    {
        logger.debug { "before merge, primary   = ${toString()}" }
        logger.debug { "before merge, secondary = $secondary" }

        logger.debug { "primary chanceToHit     = ${ chanceToHit.select(avgMinMaxSelection) }" }
        logger.debug { "secondary chanceToHit   = ${ secondary.chanceToHit.select(secondary.avgMinMaxSelection) }" }
        logger.debug { "probable chanceToHit    = ${
            probableResult (chanceToHit.select(avgMinMaxSelection),
                secondary.chanceToHit.select(secondary.avgMinMaxSelection), chanceOfSuccess)
        }" }

        return AttackResult (numTargets,
          toAvg (probableResult (chanceToHit.select(avgMinMaxSelection),    secondary.chanceToHit.select(secondary.avgMinMaxSelection), chanceOfSuccess)),
          toAvg (probableResult (damagePerHit.select(avgMinMaxSelection),   secondary.damagePerHit.select(secondary.avgMinMaxSelection), chanceOfSuccess)),
          toAvg (probableResult (damagePerRound.select(avgMinMaxSelection), secondary.damagePerRound.select(secondary.avgMinMaxSelection), chanceOfSuccess)),
          toAvg (probableResult (duration.select(avgMinMaxSelection),         secondary.duration.select(secondary.avgMinMaxSelection), chanceOfSuccess)),
          toAvg (probableResult (damageFullEffect.select(avgMinMaxSelection), secondary.damageFullEffect.select(secondary.avgMinMaxSelection), chanceOfSuccess)),
          character, attack, startCondition, turnId, actionId, effectId, spellAttack)
    }

    fun dpr(): Float {
        return damagePerRound.select (avgMinMaxSelection)
    }

    fun update(turnId: Int, actionId: Int, effectId: Int, spellAttack: SpellAttack? = null) {
        this.turnId = turnId
        this.actionId = actionId
        this.effectId = effectId
        this.spellAttack = spellAttack
    }

    fun getValue(field: AttackResultField): Any {
        val meleeOrRangeAction: MeleeOrRangeAction =
            if (this.attack.action is Weapon) { this.attack.action as Weapon } else spellAttack!!

        val saveAbility = spellAttack?.getSaveAbility()

        return when (field) {
            level           -> character.getLevel()
            characterName   -> character.getName()
            spellSaveDC     -> character.getSpellSaveDC()

            monsterName -> this.attack.monster.name
            monsterAC   -> this.attack.monster.getAC()

            damageDice  -> meleeOrRangeAction.getDamageDice()
            damageBonus -> meleeOrRangeAction.getBonusDamage(character, this.attack.isBonusAction ?: false)
            attackBonus -> meleeOrRangeAction.getBonusToHit(character, this.attack.isBonusAction ?: false)

            spellSaveAbility -> saveAbility ?: ""
            targetSaveBonus  -> if (saveAbility == null) "" else this.attack.monster.getAbilityModifier(saveAbility)

            turn        -> this.turnId
            action      -> if (this.attack.isBonusAction == true) "BA" else ""+this.actionId
            effect      -> this.effectId

            AttackResultField.attack -> if (spellAttack != null) spellAttack.toString() else this.attack.getLabel()

            AttackResultField.startCondition -> Globals.wrapWithQuotes(this.startCondition)
            AttackResultField.numTargets -> this.numTargets

            AttackResultField.chanceToHit -> this.chanceToHit.select(avgMinMaxSelection)
            AttackResultField.damagePerHit -> this.damagePerHit.select(avgMinMaxSelection)
            AttackResultField.duration -> this.duration.select(avgMinMaxSelection)
            fullEffectDamage -> this.damageFullEffect.select(avgMinMaxSelection)
            else -> {
                println("WARNING: unhandled field: $field")
                Exception("warning").printStackTrace()
            }
        }
    }
}

