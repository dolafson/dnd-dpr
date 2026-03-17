@file:OptIn(ExperimentalSerializationApi::class)

package com.vikinghelmet.dnd.dpr.modified

import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@Serializable
data class StatBlock(var str: Int, var dex: Int, var con: Int, var int: Int, var wis: Int, var cha: Int) {

    fun copyValues(other: StatBlock) {
        str = other.str
        dex = other.dex
        con = other.con
        int = other.int
        wis = other.wis
        cha = other.cha
    }

    fun getByAbilityType(abilityType: AbilityType): Int {
        return when (abilityType) {
            AbilityType.Strength -> str
            AbilityType.Dexterity -> dex
            AbilityType.Constitution -> con
            AbilityType.Intelligence -> int
            AbilityType.Wisdom -> wis
            AbilityType.Charisma -> cha
            else -> 0
        }
    }

    fun getValue(label: String): Int {
        return when (label) {
            "STR" -> str
            "DEX" -> dex
            "CON" -> con
            "INT" -> int
            "WIS" -> wis
            "CHA" -> cha
            else -> 0
        }
    }

    fun setValue(label: String, newValue: Int) {
        when (label) {
            "STR" -> str = newValue
            "DEX" -> dex = newValue
            "CON" -> con = newValue
            "INT" -> int = newValue
            "WIS" -> wis = newValue
            "CHA" -> cha = newValue
            else -> {}
        }
    }

}
