package com.vikinghelmet.dnd.dpr.turn

import com.vikinghelmet.dnd.dpr.character.Character
import com.vikinghelmet.dnd.dpr.character.inventory.Weapon
import com.vikinghelmet.dnd.dpr.spells.SpellAttack
import com.vikinghelmet.dnd.dpr.turn.AttackResultField.*
import com.vikinghelmet.dnd.dpr.util.Globals
import com.vikinghelmet.dnd.dpr.util.Globals.probableResult
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
) {
    @Transient private val logger = LoggerFactory.get(AttackResult::class.simpleName ?: "")

    override fun toString(): String = "($turnId,$actionId,$effectId): chanceToHit=$chanceToHit, damagePerRound=$damagePerRound"

    fun select(advantageProbability: Float) {
        chanceToHit.select(advantageProbability)
        damagePerHit.select(advantageProbability)
        damagePerRound.select(advantageProbability)
        duration.select(advantageProbability)
        damageFullEffect.select(advantageProbability)
    }

    fun merge(secondary: AttackResult, chanceOfSuccess: Float): AttackResult
    {
        logger.debug { "before merge, primary   = ${toString()}" }
        logger.debug { "before merge, secondary = $secondary" }

        logger.debug { "primary chanceToHit     = ${ chanceToHit.final }" }
        logger.debug { "secondary chanceToHit   = ${ secondary.chanceToHit.final }" }
        logger.debug { "probable chanceToHit    = ${
            probableResult (chanceToHit.final,
                secondary.chanceToHit.final, chanceOfSuccess)
        }" }

        return AttackResult (numTargets,
          AvgMinMax (0f,0f,0f,probableResult (chanceToHit.final,    secondary.chanceToHit.final, chanceOfSuccess)),
          AvgMinMax (0f,0f,0f,probableResult (damagePerHit.final,   secondary.damagePerHit.final, chanceOfSuccess)),
          AvgMinMax (0f,0f,0f,probableResult (damagePerRound.final, secondary.damagePerRound.final, chanceOfSuccess)),
          AvgMinMax (0f,0f,0f,probableResult (duration.final,         secondary.duration.final, chanceOfSuccess)),
          AvgMinMax (0f,0f,0f,probableResult (damageFullEffect.final, secondary.damageFullEffect.final, chanceOfSuccess)),
          character, attack, startCondition, turnId, actionId, effectId, spellAttack)
    }

    fun dpr(): Float {
        return damagePerRound.final
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

            AttackResultField.chanceToHit -> this.chanceToHit.final
            AttackResultField.damagePerHit -> this.damagePerHit.final
            AttackResultField.duration -> this.duration.final
            fullEffectDamage -> this.damageFullEffect.final
            else -> {
                println("WARNING: unhandled field: $field")
                Exception("warning").printStackTrace()
            }
        }
    }
}

