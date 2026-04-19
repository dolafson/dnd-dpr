package com.vikinghelmet.dnd.dpr

import com.vikinghelmet.dnd.dpr.scenario.ScenarioResult
import kotlinx.serialization.Serializable

@Serializable
class SimpleResult(
    val totalDPR: Int,
    var attacks: List<List<String>>? = emptyList(),
    val clone: Int? = null,
    val level: Int? = null,
)
{
    constructor(sr: ScenarioResult) : this(sr.totalDPR.toInt(), sr.getAttackNames())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as SimpleResult
        return totalDPR == other.totalDPR && attacks == other.attacks
    }

    override fun hashCode(): Int {
        var result = totalDPR
        result = 31 * result + attacks.hashCode()
        return result
    }

    override fun toString(): String {
        return "(totalDPR=$totalDPR, attacks=$attacks)"
    }
}