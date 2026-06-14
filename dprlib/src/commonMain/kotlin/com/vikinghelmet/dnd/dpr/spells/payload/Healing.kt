package com.vikinghelmet.dnd.dpr.spells.payload

import com.vikinghelmet.dnd.dpr.util.DiceBlock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Healing")
data class Healing(
    val ability: String, // auto | none
    val isTemp: Boolean,
    val _bonus: Int? = null,
    val diceCount: Int? = null,
    val diceSize: String? = null ,
) : Payload() {
    var healingDice: DiceBlock = DiceBlock("${ (diceCount?: 0) }d${diceSize?: 4}")

    constructor(dice: DiceBlock) : this("auto", false) {
        healingDice = dice
    }

    init {
        if (_bonus != null) {
            healingDice.addBonus(_bonus)
        }
    }

}