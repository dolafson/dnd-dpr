package com.vikinghelmet.dnd.dpr

import kotlinx.serialization.Serializable

@Serializable
data class Attack(
    val preconditions: Preconditions? = null,
    val monster: String, // after monster DB lookup, we will extract targetSaveBonus and isTargetEvasive
    val spell: String
) {
    @Serializable
    data class Preconditions (
        // If the target has a bonus to their saving throw determined by a die roll (such as from the Bless spell) then enter those dice here.
        val bonusDiceToSave: DiceBlock? = null,

        // If the target has a penalty to their saving throw determined by a die roll (such as by the Bane spell) then enter those dice here.
        val penaltyDiceToSave: DiceBlock? = null,

        // If you get a bonus on only one hit or target, such as with the Evoker's "Empowered Evocation" ability, you can enter the bonus here.
        // we may be able to extract this from spell properties ?
        val bonusDamage: Int? = 0,
        val bonusDamageOnFirstHit: DiceBlock? = null
    )
}