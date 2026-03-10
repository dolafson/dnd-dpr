package com.vikinghelmet.dnd.dpr.turn

import com.vikinghelmet.dnd.dpr.character.Character
import com.vikinghelmet.dnd.dprlib.util.DiceBlock

interface MeleeOrRangeAction {
    fun getBonusDamage(character: Character, isBonusAction: Boolean): Int
    fun getBonusToHit(character: Character, isBonusAction: Boolean): Int
    fun getDamageDice(): DiceBlock
}