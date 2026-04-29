package com.vikinghelmet.dnd.dpr

import com.vikinghelmet.dnd.dpr.scenario.ScenarioResult
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
class SimpleResult(
    var level: Int? = null,
    val totalDamage: Int,
    var attacks: List<List<String>>? = emptyList(),
    val clone: Int? = null,
)
{
    constructor(totalDamage: Int, attacks: List<List<String>>) : this(null, totalDamage, attacks)

    constructor(sr: ScenarioResult) : this(null, sr.totalDamage.toInt(), sr.getAttackNames())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as SimpleResult
        return totalDamage == other.totalDamage && attacks == other.attacks
    }

    override fun hashCode(): Int {
        var result = totalDamage
        result = 31 * result + attacks.hashCode()
        return result
    }

    fun toJsonPretty(): String {
        val buf = StringBuilder()
        attacks?.forEach {
            if (buf.isNotEmpty()) { buf.append(",\n")}
            buf.append("    ${ Json.encodeToString(it) }")
        }
        return "  { \"level\": $level, \"totalDamage\": $totalDamage, \"attacks\": [\n${buf}\n ]}"
    }

    override fun toString(): String {
        return "(totalDamage=$totalDamage, attacks=$attacks)"
    }

}