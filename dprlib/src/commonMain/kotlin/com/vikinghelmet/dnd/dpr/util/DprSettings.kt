package com.vikinghelmet.dnd.dpr.util

import kotlinx.serialization.Serializable

@Serializable
data class DprSettings(
    var combatantA: String = "",
    var combatantB: String = "",
    var proximity: Int = 0
) {
}
