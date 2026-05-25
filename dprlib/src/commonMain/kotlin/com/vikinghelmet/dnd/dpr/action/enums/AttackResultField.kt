package com.vikinghelmet.dnd.dpr.action.enums

// TODO: abilities: Str, Dex, ... ?
enum class AttackResultField(val constantAcrossTurns: Boolean = false)
{
    attackerName(true),level(true),

    turn,action,effect,attack,fullEffectDamage,//scenario,

    targetName(true),targetAC(true),
    damageList, attackBonus,
    spellSaveDC(true),spellSaveAbility,targetSaveBonus,startCondition,startEffects,
    numTargets,chanceToHit,damagePerHit,duration,
    ;
}