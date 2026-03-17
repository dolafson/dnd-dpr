package com.vikinghelmet.dnd.dpr.util

interface HasNumericRangeMap {
    fun getNumericRangeMap(): NumericRangeMap
    // fun getNumericRangeMap(): Map<String, NumericRange>
}

class NumericRange(val min: Int, val max: Int, val current: Int? = min) {
    override fun toString(): String {
        return "(min=$min, current=$current, max=$max)"
    }
}

class NumericRangeMap(val isEditable: Boolean, val map: Map<String, NumericRange> = mutableMapOf())

// value class NumericRangeMap(val value: Map<String, NumericRange>)