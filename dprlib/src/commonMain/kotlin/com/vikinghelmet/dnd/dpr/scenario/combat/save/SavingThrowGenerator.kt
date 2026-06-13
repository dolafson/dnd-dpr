package com.vikinghelmet.dnd.dpr.scenario.combat.save

import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import com.vikinghelmet.dnd.dpr.scenario.combat.CombatantWithStatus

class SavingThrowGenerator() {

    private fun roll(): Int { // this method may get mocked during test
        return  (1..20).random()
    }

    fun makeSavingThrow (target: CombatantWithStatus, spellSaveDC: Int, saveAbility: AbilityType): Boolean  {
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
}