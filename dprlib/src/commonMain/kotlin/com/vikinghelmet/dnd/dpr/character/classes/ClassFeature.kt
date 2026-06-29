package com.vikinghelmet.dnd.dpr.character.classes

import com.vikinghelmet.dnd.dpr.util.Globals
import kotlinx.serialization.Serializable

@Serializable
enum class ClassFeature {
    Subclass,
    DivineDomain,
    Evasive,
    AbilityScoreImprovement,
    ExtraAttack,
    UnarmoredDefense,
    FightingStyle,
    DreadAmbusher, // GloomStalker level 3
    ;

    companion object {
        fun fromName(name: String) = entries.firstOrNull { Globals.addWStoCamelCase(it.name) == name }
    }
}

