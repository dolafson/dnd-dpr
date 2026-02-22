package com.vikinghelmet.dnd.dpr.spells

import com.vikinghelmet.dnd.dpr.DiceBlock
import com.vikinghelmet.dnd.dpr.spells.payload.Attack
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

    fun getDamage(): DiceBlock {
        val dice = DiceBlock(0, 0, 0, 0, 0)

        //  "Damage": "2d6"  ... first = numberOfDice = [1..20];  second = typeOfDie = [4,6,8,10,12]
        val damage = properties.Damage ?: return dice
        val damageList = damage.split("d")
        val diceCount = damageList[0].toInt()

        when (damageList[1]) {
            "4" -> dice.four = diceCount
            "6" -> dice.six = diceCount
            "8" -> dice.eight = diceCount
            "10" -> dice.ten = diceCount
            "12" -> dice.twelve = diceCount
        }
        return dice
    }

    fun getFirstAttackSave(): Attack.Save? {
        val data = properties.dataDatarecords ?: return null
        // println(dataRecordList)
        for (d in data) {
            if (d.payload is Attack) {
                return d.payload.save
            }
        }
        return null
    }

    fun getSpellSaveResult(): List<SaveResult> {
        val result = mutableListOf<SaveResult>()
        val data = properties.dataDatarecords ?: return result
        // println(dataRecordList)
        for (d in data) {
            if (d.payload is Attack) {
                if (d.payload.save != null) {
                    val attackResult = d.payload.save.getSaveResult()
                    if (attackResult != SaveResult.NOT_APPLICABLE) {
                        result.add(attackResult)
                    }
                }
            }
        }

        return result
    }


    fun isAreaOfEffectBig(): Boolean {
        val data = properties.dataDatarecords ?: return false
        // println(dataRecordList)
        for (d in data) {
            //println(d)
            if (d.payload is Attack) {
                //println("attackPayload: "+attackPayload)
                // println("aoe: " + d.payload.aoe)

                if (d.payload.aoe != null) {
                    return d.payload.aoe.isBig()
                }
            }
        }

        return false
    }
}