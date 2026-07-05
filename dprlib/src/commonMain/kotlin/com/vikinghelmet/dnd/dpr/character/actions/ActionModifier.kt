package com.vikinghelmet.dnd.dpr.character.actions

import com.vikinghelmet.dnd.dpr.action.Damage
import com.vikinghelmet.dnd.dpr.action.enums.DamageType
import com.vikinghelmet.dnd.dpr.scenario.TargetEffectCause
import com.vikinghelmet.dnd.dpr.util.DiceBlock
import com.vikinghelmet.dnd.dpr.util.Globals

enum class ActionModifier(val supported: Boolean = false) : TargetEffectCause {
    // WH = Weapon Hit
    // TS = Target Spacing (2nd target w/in TS feet of 1st)
    // SD = Superiority Dice
    // LR = Long Rest
    // SR = Short Rest

    // EA = Extra Attack on WH if TS=X
    // EC = Extra Condition on failed saving throw
    // ED = Extra Damage

    // racial
    BreathWeapon,           // attack: 1/turn, max=PB/LR; replaces 1 attack (can combine with other attacks)
    HellishRebuke,          // reaction: creature DEX save fail -> 2d10 fire damage

    // weapon mastery
    Cleave(true),           // WH: 1/turn; EA=5 // TODO: move this usage to MasteryProperty ?

    // barbarian
    Rage,                   // BA: max=3/LR, 1/SR (table); ED=2 (table), damage resistance; adv on STR checks/saves

    // cleric
    ChannelDivinity(true),  // action: 1/turn; max=1/SR (table)  // TODO: currently in SpellsWithComplexRules

    // rogue
    SneakAttack,            // WH: 1/turn; ED=1d6 (table) if you have adv and weapon is Finesse/Ranged OR if ally TS=5 and no disadv
    CunningAction,          // BA: Dash, Disengage, or Hide
    SteadyAim,              // BA: gain adv on next attack of current turn; 0 movement before/after attack

    // fighter
    SecondWind,                 // BA: restoreHP = 1d10 + level; max=2/LR, 1/SR (table)
    ManeuverParry,              // reaction: reduce damage by SD + (STR or DEX mod)
    ManeuverDistractingStrike,  // WH: 1/attack: increase damage by SD; next attack on target gains adv
    ManeuverPrecisionAttack,    // afterAttack: 1/attack: on a miss, add SD to attack roll (potentially turn miss into hit)

    // ranger
    //HuntersMark(true),      // BA: ED=1d6; max=2/LR; max inc by level (table) ... handled via spell list
    HuntersLore,            // 1/HuntersMark; no direct impact to combat
    HordeBreaker,           // WH: 1/turn; EA=5, if 2nd not attacked by you this turn
    ColossusSlayer(true),   // WH: 1/turn; ED = 1d8 - only if creature was missing HP
    DreadfulStrike(true),   // WH: 1/turn, max=WIS/LR; ED = 2d6 psychic damage; aka "DS"
    PolarStrikes(true),     // WH: 1/turn; ED = 1d4 cold

    // ranger L11
    SuperiorHuntersPrey,    // afterAttack: 1/turn; apply HM damage to a 2nd creature if TS=30

    StalkersFlurry,         // 1/DS ; ED: 2d6 -> 2d8
    SuddenStrike,           // 1/DS ; EA=5
    MassFear,               // 1/DS ; EC WIS: target + all TS=10 -> Frightened until start of your next turn

    ChillingRetribution     // reaction: EC WIS -> Stunned until end of your next turn

    ;

    override fun toString() = Globals.addWStoCamelCase(name)

    // methods indicating when a modifier is applied

    fun givesExtraAttack() = listOf(Cleave, HordeBreaker, SuddenStrike).contains(this)

    fun isAttack()      = listOf(BreathWeapon)
    fun isAction()      = listOf(ChannelDivinity)
    fun isBonusAction() = listOf(Rage, CunningAction, SteadyAim, SecondWind).contains(this) // HuntersMark
    fun isReaction()    = listOf(HellishRebuke, ManeuverParry, ChillingRetribution).contains(this)

    fun onWeaponHit()   = listOf(Cleave, SneakAttack, HordeBreaker, ColossusSlayer, DreadfulStrike, PolarStrikes).contains(this)
    fun onWeaponMiss()  = listOf(ManeuverPrecisionAttack).contains(this)
    fun withDS()        = listOf(StalkersFlurry, SuddenStrike, MassFear).contains(this)

    fun getDamage(): Damage {
        when (this) {
            ColossusSlayer  -> return Damage(DiceBlock("1d8"), 0,0, DamageType.piercing) // TODO: should be same as weapon used
            DreadfulStrike  -> return Damage(DiceBlock("2d6"), 0,0, DamageType.psychic)
            PolarStrikes    -> return Damage(DiceBlock("1d4"), 0,0, DamageType.cold)
            else            -> return Damage(DiceBlock(),      0,0, DamageType.undefined)
        }
    }

    companion object {
        fun fromName(name: String) = entries.firstOrNull { it.name == Globals.removeNonAlpha(name) }
    }
}