package com.vikinghelmet.dnd.dpr.character.race

enum class RacialTrait(val id: Int, val traitName: String) {
    // https://www.dndbeyond.com/sources/dnd/phb-2014/races#HalflingTraits
    // https://www.dndbeyond.com/sources/dnd/phb-2024/character-origins#Halfling
    Luck(13856136, "Luck"),

    // https://www.dndbeyond.com/sources/dnd/xgte/character-options-racial-feats#ElvenAccuracy
    ElvenAccuracy(0, "Elven Accuracy") // TODO: find trait id
}