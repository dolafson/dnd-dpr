package com.vikinghelmet.dnd.dpr.character

import com.vikinghelmet.dnd.dpr.character.actions.ActionAdded
import com.vikinghelmet.dnd.dpr.character.actions.ActionModifier
import com.vikinghelmet.dnd.dpr.character.inventory.Weapon
import com.vikinghelmet.dnd.dpr.scenario.ActionsAvailable

interface Combatant {
    fun getAC(): Int

    fun getName(): String
    fun getLevel(): Int

    // TRAITS, ABILITIES, and FEATS

    fun isLucky(): Boolean
    fun isElvenAccuracy(): Boolean
    fun isElementalAdept(): Boolean
    fun isGreatWeaponFighting(): Boolean

    // COMBAT MODIFIERS

    fun getAttackBonus(w: Weapon): Int
    fun getDamageBonus(w: Weapon, isBA: Boolean): Int

    // WEAPONS

    fun getWeaponList(): List<Weapon>

    // SPELLS

    fun getSpellBonusToHit(): Int
    fun getSpellSaveDC(): Int

    // ACTIONS (spells or weapons)

    fun getActionsAvailable(): ActionsAvailable

    fun getActionModifiersAvailable(): List<ActionModifier>

    fun getActionList(): List<ActionAdded>

    // ----------------------------------------------------------------------------------------
    // CLASS INFO

    fun getExtraAttacks(): Int
}
