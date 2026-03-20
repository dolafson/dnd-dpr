package com.vikinghelmet.dnd.dpr.character.race

import com.vikinghelmet.dnd.dpr.util.Globals

enum class RacialTrait {
    // https://www.dndbeyond.com/sources/dnd/phb-2024/character-origins#HalflingTraits
    // https://www.dndbeyond.com/sources/dnd/phb-2024/character-origins#Halfling
    Luck,

    // https://www.dndbeyond.com/sources/dnd/xgte/character-options-racial-feats#ElvenAccuracy
    ElvenAccuracy
    ;
    fun getNameWithWS(): String {
        return Globals.addWStoCamelCase(name)
    }

}