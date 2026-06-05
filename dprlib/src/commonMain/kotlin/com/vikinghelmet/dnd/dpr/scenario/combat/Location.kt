package com.vikinghelmet.dnd.dpr.scenario.combat

import kotlin.math.pow
import kotlin.math.sqrt

data class Location(var x: Int, var y: Int) {
    constructor(onTeamA: Boolean): this(
        (1..4).random() * (if (onTeamA) -1 else 1),
        (-2..2).random()
    )

    fun distance(otherLocation: Location): Double {
        return sqrt( (otherLocation.x - x).toDouble().pow(2.0) +
                (otherLocation.y - y).toDouble().pow(2.0))
    }

    fun moveTowardLocation(other: Location, maxMoves: Int) {
        for (i in 1..maxMoves) {
            if (x < other.x -1) x++
            else if (x > other.x +1) x--
            else if (y < other.y -1) y++
            else if (y > other.y +1) y--
            else break
        }
    }

    fun getOneOff(): List<Location> {
        return listOf(
            Location(x-1, y-1), Location(x, y-1), Location(x+1, y-1),
            Location(x-1, y),                     Location(x+1, y),
            Location(x-1, y+1), Location(x, y+1), Location(x+1, y+1),
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Location

        if (x != other.x) return false
        if (y != other.y) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        return result
    }
}
