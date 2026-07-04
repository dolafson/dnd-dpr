package com.vikinghelmet.dnd.dpr.character.classes

import com.vikinghelmet.dnd.dpr.util.Globals
import kotlinx.serialization.Serializable

@Serializable
enum class ClassFeature {
    Subclass,
    /*
Eldir  , enabled = [Core Wizard Traits, Spellcasting, Ritual Adept, Arcane Recovery, Scholar, Wizard Subclass]
Kael   , enabled = [Spellcasting, Divine Domain, Channel Divinity, Proficiencies, Hit Points]
Lars   , enabled = [Core Rogue Traits, Expertise, Sneak Attack, Thieves’ Cant, Weapon Mastery, Cunning Action, Rogue Subclass, Steady Aim]
Leif   , enabled = [Core Ranger Traits, Spellcasting, Favored Enemy, Weapon Mastery, Deft Explorer, Fighting Style, Ranger Subclass]
Oleg   , enabled = [Rage, Unarmored Defense, Weapon Mastery, Danger Sense, Reckless Attack, Barbarian Subclass, Primal Knowledge, Core Barbarian Traits]
Rhogar , enabled = [Core Fighter Traits, Fighting Style, Second Wind, Weapon Mastery, Action Surge, Tactical Mind, Fighter Subclass]
     */
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

