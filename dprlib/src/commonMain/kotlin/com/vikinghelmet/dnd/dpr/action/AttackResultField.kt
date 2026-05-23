package com.vikinghelmet.dnd.dpr.action

// TODO: abilities: Str, Dex, ... ?
enum class AttackResultField(val constantAcrossTurns: Boolean = false)
{
    characterName(true),level(true),

    turn,action,effect,attack,fullEffectDamage,//scenario,

    monsterName(true),monsterAC(true),
    damageList, attackBonus,
    spellSaveDC(true),spellSaveAbility,targetSaveBonus,startCondition,startEffects,
    numTargets,chanceToHit,damagePerHit,duration,
    ;
}