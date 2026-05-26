package com.vikinghelmet.dnd.dpr.monsters.actions

import kotlinx.serialization.Serializable

@Serializable
data class LegendaryAction(
    val attack_bonus: Int = 0,
) : MonsterAction()