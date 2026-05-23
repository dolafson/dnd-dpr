package com.vikinghelmet.dnd.dpr.util
import kotlinx.serialization.Serializable


@Serializable
data class DiceBlock(var d4: Int, var d6: Int, var d8: Int, var d10: Int, var d12: Int)
{
    constructor(): this(0,0,0,0,0)

    constructor(diceString: String): this(0,0,0,0,0) {
        //  "Damage": "2d6"  ... first = numberOfDice = [1..20];  second = typeOfDie = [4,6,8,10,12]
        val damage = diceString
        val damageList = damage.split("d")
        val diceCount = damageList[0].toInt()
        when (damageList[1]) {
            "4" -> d4 = diceCount
            "6" -> d6 = diceCount
            "8" -> d8 = diceCount
            "10" -> d10 = diceCount
            "12" -> d12 = diceCount
        }
    }

    override fun toString(): String {
        val buf = StringBuilder()
        if (d4 != 0) buf.append("${d4}d4")
        if (d6 != 0) buf.append("${d6}d6")
        if (d8 != 0) buf.append("${d8}d8")
        if (d10 != 0) buf.append("${d10}d10")
        if (d12 != 0) buf.append("${d12}d12")
        return buf.toString()
    }

    operator fun get(i: Int): Int {
        return when(i) {
            0 -> d4
            1 -> d6
            2 -> d8
            3 -> d10
            4 -> d12
            else -> throw IndexOutOfBoundsException("input not in 0..4")
        }
    }

    fun min(): Int {
        return (d4 + d6 + d8 + d10 + d12)
    }

    fun max(): Int {
        return (d4*4 + d6*6 + d8*8 + d10*10 + d12*12)
    }

    fun isEmpty(): Boolean {
        return min() == 0
    }

    fun double(): DiceBlock {
        return DiceBlock(d4*2, d6*2, d8*2, d10*2, d12*2)
    }

    operator fun plusAssign(other: DiceBlock) {
        d4 += other.d4
        d6 += other.d6
        d8 += other.d8
        d10 += other.d10
        d12 += other.d12
    }

}
