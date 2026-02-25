package com.vikinghelmet.dnd.dpr.weapons

import com.vikinghelmet.dnd.dpr.DiceBlock
import com.vikinghelmet.dnd.dpr.DiceBlockHelper
import kotlinx.serialization.Serializable

@Serializable
data class Weapon (
    val name: String,
    val damage: String? = null,

    // data from dndbeyond character sheet

    val properties: List<String>? = null,
    val magic: Boolean? = false,
    val attackType: Int? = 1,    // 1=melee, 2=range
    val range: Int? = 5,
    val longRange: Int? = 5,
) {
    fun getDamageDice(): DiceBlock {
        return DiceBlockHelper.getDiceBlock(damage)
    }
}
