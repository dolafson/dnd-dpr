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

    override fun toString() = Globals.addWStoCamelCase(name)

    companion object {
        fun fromName(name: String) = entries.firstOrNull { it.name == Globals.removeNonAlpha(name) }
    }
}

