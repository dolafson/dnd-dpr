package com.vikinghelmet.dnd.dpr.util

import kotlinx.serialization.Serializable

@Serializable
data class Party(
    var party: List<String>,
) {
}
