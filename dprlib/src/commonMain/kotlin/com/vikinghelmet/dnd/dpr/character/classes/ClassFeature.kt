package com.vikinghelmet.dnd.dpr.character.classes

import com.vikinghelmet.dnd.dpr.scenario.TargetEffectCause
import com.vikinghelmet.dnd.dpr.util.Globals
import kotlinx.serialization.Serializable

@Serializable
enum class ClassFeature : TargetEffectCause {
    // Common features
    Subclass,
    AbilityScoreImprovement,
    ExtraAttack,
    FightingStyle,

    // Wizard
    EvocationSavant, PotentCantrip, RitualAdept, ArcaneRecovery, Scholar,
    // Cleric
    BonusProficiency, DiscipleOfLife, ChannelDivinityPreserveLife, DivineDomain, ChannelDivinity, Proficiencies, HitPoints,

    // Rogue
    MageHandLegerdemain, Expertise, SneakAttack, ThievesCant, CunningAction, SteadyAim,
    Evasive, // Rogue level 7

    // Ranger
    DreadAmbusher, GloomStalkerSpells, UmbralSight, FavoredEnemy, DeftExplorer,
    // Barbarian
    Frenzy, Rage, UnarmoredDefense, DangerSense, RecklessAttack, PrimalKnowledge,
    // Fighter
    CombatSuperiority, StudentOfWar, ManeuverOptions, SecondWind, ActionSurge, TacticalMind,

    ;

    override fun toString() = Globals.addWStoCamelCase(name)

    fun givesAdvantage() = listOf(RecklessAttack).contains(this)

    companion object {
        fun fromName(name: String) = entries.firstOrNull { it.name == Globals.removeNonAlpha(name) }
    }
}

