package com.vikinghelmet.dnd.dpr.scenario.combat.location

import kotlin.math.abs

class Cone(val center: Location, val direction: Direction, val length: Int) {

    fun getPoints(): List<Location> {
        val result = when (direction) {
            Direction.up        -> upDown(1)
            Direction.upRight   -> diagonal(1,1)
            Direction.right     -> rightLeft(1)
            Direction.downRight -> diagonal(1, -1)

            Direction.down      -> upDown(-1)
            Direction.downLeft  -> diagonal(-1, -1)
            Direction.left      -> rightLeft(-1)
            Direction.upLeft    -> diagonal(-1, 1)

            Direction.upRight2   -> diagonal2(1,1)
            Direction.downRight2 -> diagonal2(1, -1)

            Direction.downLeft2  -> diagonal2(-1, -1)
            Direction.upLeft2    -> diagonal2(-1, 1)
        }
        return result.distinct()
    }

    private fun upDown(yMult: Int): List<Location> {
        val result = mutableListOf<Location>()
        for (i in 1..length) {
            val xStart = if (i==1) 0 else -1 * (i     / 2)  // left
            val xEnd   = if (i==1) 0 else  1 * ((i-1) / 2)  // right

            for (x in xStart..xEnd ) {
                for (y in i..length) {
                    result.add (Location (center.x + x, center.y + (yMult * y)))
                }
            }
        }
        return result
    }

    private fun rightLeft(xMult: Int): List<Location> {
        val result = mutableListOf<Location>()
        for (i in 1..length) {
            for (x in i..length) {
                val yEnd   = if (i==1) 0 else  1 * ((i-1) / 2)  // top
                val yStart = if (i==1) 0 else -1 * (i     / 2)  // bottom

                for (y in yStart..yEnd) {
                    result.add (Location (center.x + (xMult * x), center.y + y))
                }
            }
        }
        return result
    }

    private fun diagonal(xMult: Int, yMult: Int): List<Location> {
        val result = mutableListOf<Location>()
        for (i in 1..length) {
            for (x in i..length) {
                for (y in 1..i) {
                    result.add (Location (center.x + (xMult *  x), center.y + (yMult * y)))
                }
            }
        }
        return result
    }

    private fun diagonal2(xMult: Int, yMult: Int): List<Location> {
        val result = mutableListOf<Location>()
        for (i in 1..length) {
            for (y in i..length) {
                for (x in 1..i) {
                    result.add (Location (center.x + (xMult *  x), center.y + (yMult * y)))
                }
            }
        }
        return result
    }

    fun dump(radius: Int = 15, target: Location? = null) {
        val points = getPoints()
        dump(center, radius, points, target)
    }

    companion object {
        fun dump(center: Location, radius: Int = 15, points: List<Location>, target: Location? = null) {

            println("  FEDCBA9876543210123456789ABCDEF")
            println("")

            for (y in -1 *radius..radius) {
                val line = StringBuilder()
                line.append(abs(y).toString(16).uppercase()).append(" ")

                for (x in -1 *radius..radius) {
                    val loc = Location(x, -1 * y)
                    val count = points.count { it == loc}.toString(16).uppercase()
                    line.append(if (loc == center) "c" else if (loc == target) "X" else if (loc in points) count else ".")
                }
                println(line.toString())
            }
        }

        fun getIntersection(center: Location, target: Location, length: Int): List<Cone> {
            val result = mutableListOf<Cone>()
            for (dir in Direction.entries) {
                val cone = Cone(center, dir, length)
                val points = cone.getPoints()
                if (points.contains(target)) {
                    result.add(cone)
                }
            }
            return result
        }
    }
}