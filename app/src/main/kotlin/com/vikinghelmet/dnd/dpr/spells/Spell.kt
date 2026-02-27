package com.vikinghelmet.dnd.dpr.spells

import com.vikinghelmet.dnd.dpr.spells.payload.Attack
import com.vikinghelmet.dnd.dpr.spells.payload.Damage
import kotlinx.serialization.Serializable

// https://github.com/nick-aschenbach/dnd-data/blob/main/data/spells.json

@Serializable
data class Spell(
    val book: String,
    val description: String,
    val name: String,
    val properties: Properties,
    val publisher: String
) {
    fun is2014(): Boolean {
        return book.endsWith("(2014)")
    }

    fun isSameIn2014And2024(): Boolean {
        return book.endsWith("(2014 and 2024)")
    }

    // TODO: find a way to model spells with delayed effect, such as 2014 Hail of Thorns:
    // concentration up to 1 min, but only 1 instant of damage
    // note, in 2024 the spell was changed to Instantaneous (Bonus Action)
    fun getDuration(): Int? {
        val dur = properties.filterDuration ?: return null

        when (dur) {
            "Instantaneous" -> return 0
            "Permanent" -> return Int.MAX_VALUE
        }

        val durList = dur.split(" ")

        return durList[0].toInt() * when (durList[1]) {
            "turn" -> 1
            "min" -> 10
            "hour", "hours", "Hours" -> 600
            "Days" -> 600 * 24
            else -> 0
        }
    }

    fun getSpellAttacks(): List<SpellAttack> {
        val attackList = mutableListOf<SpellAttack>()
        val data = properties.dataDatarecords ?: return attackList
        for (d in data) {
            if (d.payload is Attack) {
                var damagePayload : Damage? = null
                // attach Damage to Attack where applicable
                for (d2 in data) {
                    if (d2.payload is Damage && d2.parent == d.name) {
                        damagePayload = d2.payload
                        break
                    }
                }
                attackList.add(SpellAttack(d.payload, damagePayload))
            }
        }

        // println("attackList = $attackList")
        return attackList
    }
}