package com.vikinghelmet.dnd.dpr.turn

data class ScenarioResult(
    val attackResults: List<AttackResult>,
    val totalDPR: Float = 0f
)