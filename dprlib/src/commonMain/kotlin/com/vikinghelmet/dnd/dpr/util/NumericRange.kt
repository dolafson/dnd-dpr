package com.vikinghelmet.dnd.dpr.util

import com.vikinghelmet.dnd.dpr.character.stats.AbilityType

class NumericRange(val min: Int, val max: Int, var current: Int = min) {
    override fun toString(): String {
        return "(min=$min, current=$current, max=$max)"
    }
}

class EditableAbilityMap(val map: Map<AbilityType, NumericRange> = mutableMapOf()) {
    override fun toString(): String {
        return map.toString()
    }
}
