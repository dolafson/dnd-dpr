package com.vikinghelmet.dnd.dpr.character.actions

import com.vikinghelmet.dnd.dpr.util.DiceBlock
import com.vikinghelmet.dnd.dpr.util.Globals

enum class ActionModifier(val supported: Boolean = false) {
    // SA = Spell-like Action
    // WH = Weapon Hit
    // TS = Target Spacing (2nd target w/in TS feet of 1st)
    // SD = Superiority Dice
    // LR = Long Rest
    // SR = Short Rest

    // EA = Extra Attack on WH if TS=X
    // EC = Extra Condition on failed saving throw
    // ED = Extra Damage

    // racial
    BreathWeapon,           // 1/turn, max=PB/LR; SA, replaces 1 attack (can combine with other attacks)
    HellishRebuke,          // reaction: creature DEX save fail -> 2d10 fire damage

    // weapon mastery
    Cleave(true),           // 1/turn; EA=5 // TODO: move this usage to MasteryProperty ?

    // barbarian
    Rage,                   // BA: max=3/LR, 1/SR (table); ED=2 (table), damage resistance; adv on STR checks/saves
    DangerSense,            // adv on DEX saves
    RecklessAttack,         // 1/turn; adv on STR attacks; opponents gain adv too

    // cleric
    ChannelDivinity(true),  // 1/turn; max=1/SR (table)  // TODO: currently in SpellsWithComplexRules

    // rogue
    SneakAttack,            // 1/turn; ED=1d6 (table) if you have adv and weapon is Finesse/Ranged OR if ally TS=5 and no disadv
    CunningAction,          // BA: Dash, Disengage, or Hide
    SteadyAim,              // BA: gain adv on next attack of current turn; 0 movement before/after attack

    // fighter
    SecondWind,             // BA: restoreHP = 1d10 + level; max=2/LR, 1/SR (table)
    Parry,                  // reaction: reduce damage by SD + (STR or DEX mod)
    DistractingStrike,      // 1/attack: increase damage by SD; next attack on target gains adv
    PrecisionAttack,        // 1/attack: on a miss, add SD to attack roll (potentially turn miss into hit)

    // ranger
    HuntersMark(true),      // BA: ED=1d6; max=2/LR; max inc by level (table)
    HuntersLore,            // 1/HuntersMark; no direct impact to combat
    HordeBreaker,           // 1/turn; EA=5, if 2nd not attacked by you this turn
    ColossusSlayer(true),   // 1/turn; ED = 1d8 on WH if creature was missing HP
    DreadfulStrike(true),   // 1/turn, max=WIS/LR; ED = 2d6 psychic damage on WH; aka "DS"
    PolarStrikes(true),     // 1/turn; ED = 1d4 cold on WH

    // ranger L11
    SuperiorHuntersPrey,    // 1/turn; apply HM damage to a 2nd creature if TS=30

    StalkersFlurry,         // 1/DS ; ED: 2d6 -> 2d8
    SuddenStrike,           // 1/DS ; EA=5
    MassFear,               // 1/DS ; EC WIS: target + all TS=10 -> Frightened until start of your next turn

    ChillingRetribution     // reaction: EC WIS -> Stunned until end of your next turn

    ;

    override fun toString(): String {
        if (this == HuntersLore) return "Hunter's Lore"
        return Globals.addWStoCamelCase(name)
    }

    fun getBonusDamage(): DiceBlock {
        when (this) {
            ColossusSlayer -> return DiceBlock("1d8")
            DreadfulStrike -> return DiceBlock("2d6")
            PolarStrikes -> return DiceBlock("1d4")
            else -> return DiceBlock()
        }
    }

    companion object {
        fun fromName(name: String): ActionModifier? {
            return entries.firstOrNull { it.toString() == name }
        }
    }
}