package com.vikinghelmet.dnd.dpr.scenario.combat.save

import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import com.vikinghelmet.dnd.dpr.scenario.combat.CombatantWithStatus
import com.vikinghelmet.dnd.dpr.spells.Spell
import com.vikinghelmet.dnd.dpr.util.AttackAdvantage
import dev.shivathapaa.logger.api.LoggerFactory
import kotlinx.serialization.Transient

class SavingThrowGenerator(val target: CombatantWithStatus)
{
    @Transient private val logger = LoggerFactory.get(SavingThrowGenerator::class.simpleName ?: "")

    private fun roll(): Int { // this method may get mocked during test
        return  (1..20).random()
    }

    fun makeSavingThrow (spellSaveDC: Int, saveAbility: AbilityType, spell: Spell?): Boolean  {
        val autoFailSave = (target.isAutoFailStrAndDexSaves() &&
                listOf(AbilityType.Strength, AbilityType.Dexterity).contains(saveAbility))

        if (autoFailSave) return false

        var saveRoll = roll()
        val advantage = target.getAdvantageOnSave(saveAbility, spell)
        when (advantage) {
            AttackAdvantage.disadvantage -> {
                logger.warn { "DisadvantageOnSave: $saveAbility" }
                saveRoll = kotlin.math.min (saveRoll, roll())
            }
            AttackAdvantage.advantage -> {
                logger.warn { "AdvantageOnSave: $saveAbility" }
                saveRoll = kotlin.math.max (saveRoll, roll())
            }
            AttackAdvantage.normal -> {}
        }

        var targetSaveBonus = target.getAbilityModifier(saveAbility)

        target.toList().forEach { targetSaveBonus += it.saveBonus.roll() - it.savePenalty.roll() } // bless & bane

        return (saveRoll + targetSaveBonus >= spellSaveDC)
    }

    // first: true if at least one saving throw was made; second: true only if first is true and the save succeeded
    fun makeSavingThrows (spellNameFunction: (String) -> Boolean, thereCanBeOnlyOne: Boolean = false): Pair<Boolean, Boolean> {
        val iter = target.iterator()
        var attempted = false
        var succeeded = false
        while (iter.hasNext()) {
            val effect = iter.next()
            if (effect.cause is Spell) {
                val spell = effect.cause as Spell
                if (spellNameFunction(spell.name)) {
                    attempted = true
                    if (makeSavingThrow(effect.spellSaveDC, effect.save!!.saveAbility, spell)) {
                        iter.remove()
                        succeeded = true
                        if (thereCanBeOnlyOne) {
                            return Pair(true, true)
                        }
                    } else if (thereCanBeOnlyOne) {
                        return Pair(true, false)
                    }
                }
            }
        }
        return Pair(attempted, succeeded)
    }
}