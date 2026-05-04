package com.vikinghelmet.dnd.dpr.turn

import com.vikinghelmet.dnd.dpr.character.Character
import com.vikinghelmet.dnd.dpr.scenario.Scenario
import com.vikinghelmet.dnd.dpr.util.DiceBlock

interface MeleeOrRangeAction {
    fun getBonusDamage(character: Character, isBonusAction: Boolean): Int
    fun getBonusToHit(character: Character, isBonusAction: Boolean): Int
    fun getDamageDice(): DiceBlock
    fun getNumTargetsAffected(scenario: Scenario): Int
}