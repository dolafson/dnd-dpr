package com.vikinghelmet.dnd.dpr.turn

// TODO: abilities: Str, Dex, ... ?
enum class AttackResultField(val constantAcrossTurns: Boolean = false)
{
    characterName(true),level(true),

    turn,action,effect,attack,fullEffectDamage,scenario,

    spellBonusToHit(true),spellSaveDC(true),monsterName(true),monsterAC(true),
    weaponDamageDice,weaponDamageBonus,weaponAttackBonus,
    spellSaveAbility,targetSaveBonus,startCondition,
    numTargets,chanceToHit,damagePerHit,duration,
    ;
}