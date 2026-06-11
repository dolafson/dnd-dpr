package com.vikinghelmet.dnd.dpr.scenario.combat

import com.vikinghelmet.dnd.dpr.action.Turn
import com.vikinghelmet.dnd.dpr.action.Weapon
import com.vikinghelmet.dnd.dpr.scenario.TargetEffect
import com.vikinghelmet.dnd.dpr.spells.Spell
import com.vikinghelmet.dnd.dpr.util.AttackAdvantage

enum class TurnOptionRanking {
    // lowest to highest

    DoNothing,

    Cantrip,

    WeaponSimple,

    WeaponWithBonusActionSpell,
    WeaponWithBonusActionSpellAOE,            // Hail of Thorns, Ice Knife
    WeaponWithBonusActionSpellRecurring,      // Hunters Mark

    WeaponWithBonusActionHide,      // Rogue, Goblin

    SpellSimple,

    // context matters ...
    // Spells with High Damage are preferred over Recurring damage, if we are fighting one big bad versus a horde of minions
    SpellWithDamage,                //

    SpellThatGivesAdvantage,        // Bless, Bane

    SpellWithRecurringDamage,       //
    SpellWithIncapcitate,           // Hold Person
    SpellWithRestoreHP,             // Cure Wounds - if team member has low HP


    // context matters ...
    // AOE spells are preferred over non-AOE, if number of targets is greater than one
    SpellWithRecurringDamageAOE,    // Conjure Animals
    SpellWithDamageAOE,             // Fireball
    SpellWithIncapcitateAOE,        // Sleep, Entangle
    SpellWithRestoreHPAOE,          //

    SpellWithLifeRestoration,       // Spare the Dying - if team member has negative HP
    ;

    fun isWeapon(): Boolean {
        return when (this) {
            WeaponSimple,
            WeaponWithBonusActionSpell,
            WeaponWithBonusActionSpellAOE,            // Hail of Thorns, Ice Knife
            WeaponWithBonusActionSpellRecurring,      // Hunters Mark

            WeaponWithBonusActionHide ->  true

            else -> false
        }
    }

    fun isSpell(): Boolean = (this != DoNothing) && !isWeapon()


    companion object {

        private fun fromTurnWithWeapon(turn: Turn): TurnOptionRanking {
            val bonusActionSpell = turn.attacks.firstOrNull { it.action is Spell && it.isBonusAction == true }
            if (bonusActionSpell != null) {
                val spell = bonusActionSpell.action as Spell
                if (spell.isRecurring()) {
                    return WeaponWithBonusActionSpellRecurring
                } else if (spell.isAOE()) {
                    return WeaponWithBonusActionSpellAOE
                } else {
                    return WeaponWithBonusActionSpell
                }
            }

            // TODO: WeaponWithBonusActionHide,      // Rogue, Goblin

            return WeaponSimple
        }

        private fun fromTurnWithSpell(turn: Turn): TurnOptionRanking {
            val spell = turn.getSpell()!!

            // best spell
            if (spell.name.equals("Spare the Dying")) {
                return SpellWithLifeRestoration
            }

            // worst spell
            if (spell.isCantrip()) {
                return Cantrip
            }

            if (spell.isHealing()) {
                return if (spell.isAOE()) SpellWithRestoreHPAOE else SpellWithRestoreHP
            }

            val targetEffect = TargetEffect(0, spell)
            if (targetEffect.attacksAgainstMe == AttackAdvantage.advantage) {
                return SpellThatGivesAdvantage
            }

            if (targetEffect.unableToAct) {
                return if (spell.isAOE()) SpellWithIncapcitateAOE else SpellWithIncapcitate
            }

            if (! spell.incursDamage()) {
                return SpellSimple
            }

            if (spell.isRecurring()) {
                return if (spell.isAOE()) SpellWithRecurringDamageAOE else SpellWithRecurringDamage
            }

            return if (spell.isAOE()) SpellWithDamageAOE else SpellWithDamage
        }

        fun fromTurn(turn: Turn): TurnOptionRanking {
            if (turn.attacks.isEmpty()) {
                return DoNothing
            }

            if (turn.attacks.any { it.action is Weapon }) {
                return fromTurnWithWeapon(turn)
            }

            return fromTurnWithSpell(turn)
        }
    }
}