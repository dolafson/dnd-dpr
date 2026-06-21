package com.vikinghelmet.dnd.dpr.scenario.combat.save

import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import com.vikinghelmet.dnd.dpr.scenario.combat.CombatantWithStatus
import com.vikinghelmet.dnd.dpr.spells.Spell

class SavingThrowGenerator(val target: CombatantWithStatus)
{
    private fun roll(): Int { // this method may get mocked during test
        return  (1..20).random()
    }

    fun makeSavingThrow (spellSaveDC: Int, saveAbility: AbilityType): Boolean  {
        val autoFailSave = (target.isAutoFailStrAndDexSaves() &&
                listOf(AbilityType.Strength, AbilityType.Dexterity).contains(saveAbility))

        if (autoFailSave) return false

        var saveRoll = roll()
        if (target.getDisadvantageOnSave() == saveAbility) {
            saveRoll = kotlin.math.min(saveRoll, roll())
        }

        var targetSaveBonus = target.getAbilityModifier(saveAbility)

        target.toList().forEach { targetSaveBonus += it.saveBonus.roll() - it.savePenalty.roll() } // bless & bane

        return (saveRoll + targetSaveBonus >= spellSaveDC)
    }

    // return true if you had to make at least one saving throw
    fun makeSavingThrows (spellNameFunction: (String) -> Boolean, thereCanBeOnlyOne: Boolean = false): Boolean {
        val iter = target.iterator()
        var result = false
        while (iter.hasNext()) {
            val effect = iter.next()
            if (effect.cause is Spell) {
                val spell = effect.cause as Spell
                if (spellNameFunction(spell.name) &&
                    makeSavingThrow (effect.spellSaveDC, effect.save!!.saveAbility))
                {
                    iter.remove()
                    result = true
                    if (thereCanBeOnlyOne) {
                        return result
                    }
                }
            }
        }
        return result
    }

    fun saveByTakingAction(): Boolean {
        val iter = target.iterator()
        while (iter.hasNext()) {
            val effect = iter.next()
            if (effect.cause !is Spell) continue
            if (SaveByTakingAnAction.contains((effect.cause as Spell).name)) {
                if (makeSavingThrow(effect.spellSaveDC, effect.save!!.saveAbility)) {
                    iter.remove()
                }
                return true
            }
        }
        return false
    }

}