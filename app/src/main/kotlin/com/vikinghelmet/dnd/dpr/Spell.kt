package com.vikinghelmet.dnd.dpr
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// https://github.com/nick-aschenbach/dnd-data/blob/main/data/spells.json

@Serializable
data class Spell(
    val name: String,
    val description: String,
    val publisher: String,
    val book: String,
    val properties: SpellProperties
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

    fun getSpellSaveResult(): List<SpellSaveResult> {
        val result = mutableListOf<SpellSaveResult>()
        val data = properties.dataDatarecords ?: return result

        val dataRecordList: List<SpellDataRecord> = Json.decodeFromString(data)
        // println(dataRecordList)
        for (d in dataRecordList) {
            //println(d)
            val payload: SpellDataRecordPayload = Json.decodeFromString(d.payload)
            if (payload is AttackPayload) {
                val attackPayload: AttackPayload = payload
                // println(attackPayload)

                if (attackPayload.save?.onSucceed != null) {
                    val onSucceed = attackPayload.save.onSucceed
                    //println(onSucceed)

                    if (".*three times.*".toRegex().matches(onSucceed)) continue // TODO: accumulated saves

                    if (".*[Hh]alf.*amage.*".toRegex().matches(onSucceed)) {
                        result.add(SpellSaveResult.HALF_DAMAGE)
                    }
                    if (".*([Ss]pell.*[Ee]nds|[Ee]nds.*[Ss]pell).*".toRegex().matches(onSucceed)) {
                        result.add(SpellSaveResult.SPELL_ENDS)
                    }
                    if (".*([Cc]ondition.*[Ee]nd|[Ee]nd.*[Ss]pell|no longer).*".toRegex().matches(onSucceed)) {
                        result.add(SpellSaveResult.CONDITION_ENDS)
                    }
                    if (".*([Nn]o effect|unaffected|isn.t Restrained|resists your efforts|isn.t affected).*".toRegex()
                            .matches(onSucceed)
                    ) {
                        result.add(SpellSaveResult.NO_EFFECT)
                    }
                }
            }
        }

        return result
    }


    fun isAreaOfEffectBig(): Boolean {
        val data = properties.dataDatarecords ?: return false

        val dataRecordList: List<SpellDataRecord> = Json.decodeFromString(data)
        // println(dataRecordList)
        for (d in dataRecordList) {
            //println(d)
            val payload: SpellDataRecordPayload = Json.decodeFromString(d.payload)
            if (payload is AttackPayload) {
                val attackPayload: AttackPayload = payload
                //println("attackPayload: "+attackPayload)
                println("aoe: "+attackPayload.aoe)

                if (attackPayload.aoe != null) {
                    return attackPayload.aoe.isBig()
                }
            }
        }

        return false
    }
}