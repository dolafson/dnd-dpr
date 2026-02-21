package com.vikinghelmet.dnd.dpr.spells

import com.vikinghelmet.dnd.dpr.monsters.Monster
import kotlinx.serialization.Serializable

@Serializable
data class Attack(
    val monster: Monster,
    val spell: Spell
)