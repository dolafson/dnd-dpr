package com.vikinghelmet.dnd.dpr.scenario.combat

class Cone(val center: Location, val direction: Direction, val length: Int) {

    fun getPoints(): List<Location> {
        return when (direction) {
            Direction.up        -> upDown(1)
            Direction.upRight   -> diagonal(1,1)
            Direction.right     -> rightLeft(1)
            Direction.downRight -> diagonal(1, -1)

            Direction.down      -> upDown(-1)
            Direction.downLeft  -> diagonal(-1, -1)
            Direction.left      -> rightLeft(-1)
            Direction.upLeft    -> diagonal(-1, 1)
        }
    }

    private fun upDown(yMult: Int): List<Location> {
        val result = mutableListOf<Location>()
        for (i in 1..length) {
            val xStart = if (i==1) 0 else -1 * (i     / 2)  // left
            val xEnd   = if (i==1) 0 else  1 * ((i-1) / 2)  // right

            for (x in xStart..xEnd ) {
                for (y in i..length) {
                    result.add (Location ((center.x + x), yMult * (center.y + y)))
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
                    result.add (Location (xMult * (center.x + x), center.y + y))
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
                    result.add (Location (xMult*(center.x + x), yMult*(center.y + y)))
                }
            }
        }
        return result
    }

    fun dump(radius: Int = 8) {
        val points = getPoints()
        for (y in -1 *radius..radius) {
            val line = StringBuilder()
            for (x in -1 *radius..radius) {
                line.append(if (Location(x, -1 * y) in points) "X" else ".")
            }
            println(line.toString())
        }
    }
}