package com.vikinghelmet.dnd.dpr.action

import com.vikinghelmet.dnd.dpr.character.actions.ActionAdded
import com.vikinghelmet.dnd.dpr.character.actions.ActionModifier
import com.vikinghelmet.dnd.dpr.character.feats.Feat
import com.vikinghelmet.dnd.dpr.character.race.RacialTrait
import com.vikinghelmet.dnd.dpr.character.spells.PreparedSpell
import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import com.vikinghelmet.dnd.dpr.scenario.ActionsAvailable

interface Combatant {
    fun is2014(): Boolean
    fun getAC(): Int
    fun getName(): String
    fun isFeatEnabled(requested : Feat): Boolean
    fun isRacialTraitEnabled(requested : RacialTrait): Boolean
    fun isEvasive(): Boolean

    fun getAbilityModifier(abilityType: AbilityType): Int

    fun getWeaponList(): List<Weapon>

    fun getSpellBonusToHit(): Int
    fun getSpellSaveDC(): Int

    fun getPreparedBonusActionSpells(targetProximity: Int): List<PreparedSpell>
    fun getSpellSlots(): List<Int>

    fun getActionsAvailable(): ActionsAvailable

    fun getActionModifiersAvailable(): List<ActionModifier>

    fun getActionList(): List<ActionAdded>
}