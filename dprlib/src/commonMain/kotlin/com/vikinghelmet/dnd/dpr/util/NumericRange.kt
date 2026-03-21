package com.vikinghelmet.dnd.dpr.util

class NumericRange(val min: Int, val max: Int, var current: Int = min) {
    override fun toString(): String {
        return "(min=$min, current=$current, max=$max)"
    }
}
