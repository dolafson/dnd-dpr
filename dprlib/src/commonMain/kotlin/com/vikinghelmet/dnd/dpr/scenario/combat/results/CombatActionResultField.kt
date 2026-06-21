package com.vikinghelmet.dnd.dpr.scenario.combat.results

// TODO: abilities: Str, Dex, ... ?
enum class CombatActionResultField(val constantAcrossTurns: Boolean = false)
{
    attackerName(true),
    battle(true), turn,action,effect,
    attackerLocation,
    actionTaken, damageList,

    targetName(true), targetAC(true), targetHP, deathSaves,

    endCondition, endEffects,

    //attackBonus, spellSaveDC(true), spellSaveAbility, targetSaveBonus

    ;
}