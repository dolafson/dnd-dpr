package com.vikinghelmet.dnd.dpr
import kotlinx.serialization.Serializable

@Serializable
data class DiceBlock(var d4: Int, var d6: Int, var d8: Int, var d10: Int, var d12: Int)
{
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
}
