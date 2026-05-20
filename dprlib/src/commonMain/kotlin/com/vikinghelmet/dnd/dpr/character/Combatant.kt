package com.vikinghelmet.dnd.dpr.character

import com.vikinghelmet.dnd.dpr.character.actions.ActionAdded
import com.vikinghelmet.dnd.dpr.character.actions.ActionModifier
import com.vikinghelmet.dnd.dpr.character.feats.Feat
import com.vikinghelmet.dnd.dpr.character.inventory.Weapon
import com.vikinghelmet.dnd.dpr.character.race.RacialTrait
import com.vikinghelmet.dnd.dpr.scenario.ActionsAvailable

interface Combatant {
    fun getAC(): Int
    fun getName(): String
    fun getLevel(): Int

    fun isFeatEnabled(requested : Feat): Boolean
    fun isRacialTraitEnabled(requested : RacialTrait): Boolean

    fun getAttackBonus(w: Weapon): Int
    fun getDamageBonus(w: Weapon, isBA: Boolean): Int

    fun getWeaponList(): List<Weapon>

    fun getSpellBonusToHit(): Int
    fun getSpellSaveDC(): Int

    fun getActionsAvailable(): ActionsAvailable

    fun getActionModifiersAvailable(): List<ActionModifier>

    fun getActionList(): List<ActionAdded>

    fun getExtraAttacks(): Int
}
