package com.vikinghelmet.dnd.dpr
import kotlinx.serialization.Serializable

@Serializable
data class DiceBlock(var four: Int, var six: Int, var eight: Int, var ten: Int, var twelve: Int)
{
    operator fun get(i: Int): Int {
        return when(i) {
            0 -> four
            1 -> six
            2 -> eight
            3 -> ten
            4 -> twelve
            else -> throw IndexOutOfBoundsException("input not in 0..4")
        }
    }

    fun min(): Int {
        return (four + six + eight + ten + twelve)
    }

    fun max(): Int {
        return (four*4 + six*6 + eight*8 + ten*10 + twelve*12)
    }

    fun isEmpty(): Boolean {
        return min() == 0
    }
}
