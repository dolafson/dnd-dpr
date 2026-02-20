package com.vikinghelmet.dnd.dpr
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Spell(
    val name: String,
    val description: String,
    val publisher: String,
    val book: String,
    val properties: SpellProperties
) {
    fun getDuration(): Int? {
        val dur = properties.filterDuration ?: return null

        when (dur) {
            "Instantaneous" -> return 0
            "Permanent" -> return Int.MAX_VALUE
        }

        val durList = dur.split(" ")

        val timespan = when (durList[1]) {
            "turn" -> 1
            "min" -> 10
            "hour", "hours", "Hours" -> 600
            "Days" -> 600 * 24
            else -> 0
        }

        return durList[0].toInt() * timespan
    }

    fun getDamage(): DiceBlock {
        var dice = DiceBlock(0, 0, 0, 0, 0)

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
        var result = mutableListOf<SpellSaveResult>()
        val data = properties.dataDatarecords ?: return result

        val dlist: List<DataRecord> = Json.decodeFromString(data)
        // println(dlist)
        for (d in dlist) {
            //println(d)
            val payload: DataRecordPayload = Json.decodeFromString(d.payload)
            if (payload is AttackPayload) {
                var attackPayload: AttackPayload = payload
                // println(attackPayload)

                if (attackPayload.save?.onSucceed != null) {
                    var onSucceed = attackPayload.save?.onSucceed
                    //println(onSucceed)

                    if (".*three times.*".toRegex().matches(onSucceed!!)) continue // TODO: accumulated saves

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

        val dlist: List<DataRecord> = Json.decodeFromString(data)
        // println(dlist)
        for (d in dlist) {
            //println(d)
            val payload: DataRecordPayload = Json.decodeFromString(d.payload)
            if (payload is AttackPayload) {
                var attackPayload: AttackPayload = payload
                //println("attackPayload: "+attackPayload)
                //println("aoe: "+attackPayload.aoe)

                if (attackPayload.aoe != null) {
                    return attackPayload.aoe!!.isBig()
                }
            }
        }

        return false
    }
}