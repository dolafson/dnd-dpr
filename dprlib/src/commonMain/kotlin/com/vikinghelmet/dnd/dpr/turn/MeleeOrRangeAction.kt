package com.vikinghelmet.dnd.dpr.turn

import com.vikinghelmet.dnd.dpr.character.Combatant
import com.vikinghelmet.dnd.dpr.scenario.Scenario
import com.vikinghelmet.dnd.dpr.util.DiceBlock

interface MeleeOrRangeAction {
    fun getBonusDamage(combatant: Combatant, isBonusAction: Boolean): Int
    fun getBonusToHit(combatant: Combatant, isBonusAction: Boolean): Int
    fun getDamageDice(): DiceBlock
    fun getNumTargetsAffected(scenario: Scenario): Int
}