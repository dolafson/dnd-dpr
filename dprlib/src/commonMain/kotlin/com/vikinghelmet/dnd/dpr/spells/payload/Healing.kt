package com.vikinghelmet.dnd.dpr.spells.payload

import com.vikinghelmet.dnd.dpr.util.DiceBlock
import dev.shivathapaa.logger.api.LoggerFactory
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@SerialName("Healing")
data class Healing(
    val ability: String, // auto | none
    val isTemp: Boolean,
    val _bonus: Int? = null,
    val diceCount: Int? = null,
    val diceSize: String? = null ,
    var _healingDice: DiceBlock = DiceBlock(),
) : Payload() {

    @Transient private val logger = LoggerFactory.get(Healing::class.simpleName ?: "")

    constructor(dice: DiceBlock) : this("auto", false, _healingDice = dice)

    // NOTE: in kotlin, "init" blocks are called before json deserialization ... so not used here

    fun getHealingDice(): DiceBlock {
        if (!_healingDice.isEmpty()) return _healingDice

        _healingDice = DiceBlock("${ (diceCount?: 0) }${diceSize?: 4}")
        if (_bonus != null) {
            _healingDice.addBonus(_bonus)
        }
        return _healingDice
    }

}