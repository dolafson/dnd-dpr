package com.vikinghelmet.dnd.dpr.turn

import com.vikinghelmet.dnd.dpr.util.DiceBlock
import kotlinx.serialization.Serializable

@Serializable
data class Preconditions (
    // If the target has a bonus to their saving throw determined by a die roll (such as from the Bless spell) then enter those dice here.
    var bonusDiceToSave: DiceBlock = DiceBlock(0, 0, 0, 0, 0),

    // If the target has a penalty to their saving throw determined by a die roll (such as by the Bane spell) then enter those dice here.
    var penaltyDiceToSave: DiceBlock = DiceBlock(0, 0, 0, 0, 0),

    var bonusDiceToHit: DiceBlock = DiceBlock(0, 0, 0, 0, 0),
    var penaltyDiceToHit: DiceBlock = DiceBlock(0, 0, 0, 0, 0),
    var bonusDamageDice: DiceBlock = DiceBlock(0, 0, 0, 0, 0),

    // If you get a bonus on only one hit or target, such as with the Evoker's "Empowered Evocation" ability, you can enter the bonus here.
    // we may be able to extract this from spell properties ?
    var bonusDamageOnFirstHit: DiceBlock = DiceBlock(0, 0, 0, 0, 0),

    var autoFailSave: Boolean = false, // target is paralyzed, petrified, stunned, or unconscious; fails all saves until condition ends
)