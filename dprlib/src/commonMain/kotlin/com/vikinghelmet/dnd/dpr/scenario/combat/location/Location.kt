package com.vikinghelmet.dnd.dpr.scenario.combat.location

import com.vikinghelmet.dnd.dpr.util.Constants
import dev.shivathapaa.logger.api.LoggerFactory
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.sqrt

val logger = LoggerFactory.get(Location::class.simpleName ?: "")

data class Distance(val units: Int) : Comparable<Distance> { // TODO: store units as Int instead of double ?

    constructor (loc1: Location, loc2: Location) :
            this(
                floor(
                    sqrt( (loc1.x - loc2.x).toDouble().pow(2.0) + (loc1.y - loc2.y).toDouble().pow(2.0))).toInt())

    fun toFeet() = units * Constants.DISTANCE_GRANULARITY
        //kotlin.math.round(units * Constants.DISTANCE_GRANULARITY).toInt()

    override fun compareTo(other: Distance): Int {
        return units.compareTo(other.units)
        // units.toInt().compareTo(other.units.toInt())
    }

    override fun toString(): String {
        return "${(units * Constants.DISTANCE_GRANULARITY)} ft"
    }

    companion object {
        fun melee() = Distance(1)
        fun fromFeet(value: Int) = Distance(value / Constants.DISTANCE_GRANULARITY)
    }
}

data class Location(var x: Int, var y: Int) {

    constructor(onTeamA: Boolean): this(
        (1..4).random() * (if (onTeamA) -1 else 1),
        (-2..2).random()
    )

    // NOTE: location units are in increment of 5 feet
    fun distance(otherLocation: Location) = Distance(this, otherLocation)

    fun moveTowardLocation(other: Location, maxMoves: Int) {
        val initialLocation = this.copy()
        //if (x == other.x && y == other.y) return // sharing the same space: should be avoided when possible

        // stop when you are within 1 unit
        if (! (abs(x - other.x) <= 1 && abs(y - other.y) <= 1))
        {
            for (i in 1..maxMoves) {
                if (x < other.x -1) x++ else if (x > other.x +1) x--
                if (y < other.y -1) y++ else if (y > other.y +1) y--
            }
        }

        if (this == initialLocation) {
            logger.debug { "Moving toward $other, old location = $initialLocation, no movement needed" }
        } else {
            logger.debug { "Moving toward $other, old location = $initialLocation, new location = $this" }
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

    override fun toString(): String {
        return "($x, $y)"
    }
}
