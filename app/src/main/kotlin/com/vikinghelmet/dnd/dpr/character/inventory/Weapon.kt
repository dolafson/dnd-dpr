package com.vikinghelmet.dnd.dpr.character.inventory

import com.vikinghelmet.dnd.dpr.util.DiceBlock
import com.vikinghelmet.dnd.dpr.util.DiceBlockHelper
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
    val nickname: String? = null,
) {
    fun getDamageDice(): DiceBlock {
        return DiceBlockHelper.get(damage)
    }

    fun isLight(): Boolean { // this may get used often,
        return properties?.contains("Light") == true
    }
}
