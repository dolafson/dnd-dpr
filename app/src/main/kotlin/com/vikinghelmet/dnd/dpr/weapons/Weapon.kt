package com.vikinghelmet.dnd.dpr.weapons

import com.vikinghelmet.dnd.dpr.DiceBlock
import com.vikinghelmet.dnd.dpr.DiceBlockHelper
import kotlinx.serialization.Serializable

@Serializable
class Weapon (
    val name: String,
    val damage: String? = null,

//    val description: String,
//    val properties: MonsterProperties,
//    val publisher: String,
//    val book: String,

) {
    fun getDamageDice(): DiceBlock {
        return DiceBlockHelper.getDiceBlock(damage)
    }
}
