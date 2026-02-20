package com.vikinghelmet.dnd.dpr
import kotlinx.serialization.Serializable

@Serializable
data class DiceBlock(var four: Int, var six: Int, var eight: Int, var ten: Int, var twelve: Int)
{
    fun toList(): List<Int> {
        return listOf(four, six, eight, ten, twelve)
    }

    fun min(): Int {
        return (four + six + eight + ten + twelve);
    }

    fun max(): Int {
        return (four*4 + six*6 + eight*8 + ten*10 + twelve*12);
    }

    fun isEmpty(): Boolean {
        // println ("empty ... min = "+min()+", block = "+toList())
        return min() == 0
    }
}
