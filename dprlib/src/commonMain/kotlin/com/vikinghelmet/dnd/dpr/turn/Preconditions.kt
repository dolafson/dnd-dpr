package com.vikinghelmet.dnd.dpr.turn

import com.vikinghelmet.dnd.dpr.util.DiceBlock
import kotlinx.serialization.Serializable

@Serializable
data class Preconditions (
    // If the target has a bonus to their saving throw determined by a die roll (such as from the Bless spell) then enter those dice here.
    var bonusDiceToSave: DiceBlock? = null,

    // If the target has a penalty to their saving throw determined by a die roll (such as by the Bane spell) then enter those dice here.
    var penaltyDiceToSave: DiceBlock? = null,

    //
    var bonusDiceToHit: DiceBlock? = null,

    //
    var penaltyDiceToHit: DiceBlock? = null,

    var bonusDamage: Int? = 0,
    var bonusDamageDice: DiceBlock? = null,

    // If you get a bonus on only one hit or target, such as with the Evoker's "Empowered Evocation" ability, you can enter the bonus here.
    // we may be able to extract this from spell properties ?
    var bonusDamageOnFirstHit: DiceBlock? = null,

    var autoFailSave: Boolean? = false, // target is paralyzed, petrified, stunned, or unconscious; fails all saves until condition ends
)