package com.vikinghelmet.dnd.dpr.scenario.combat

import com.vikinghelmet.dnd.dpr.action.Combatant
import com.vikinghelmet.dnd.dpr.character.PlayerCharacter
import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import com.vikinghelmet.dnd.dpr.scenario.onesided.TargetEffect
import com.vikinghelmet.dnd.dpr.spells.Spell
import com.vikinghelmet.dnd.dpr.spells.SpellsWithComplexRules.HuntersMark
import com.vikinghelmet.dnd.dpr.util.Constants
import com.vikinghelmet.dnd.dpr.util.Constants.levelToFavoredEnemyMap
import com.vikinghelmet.dnd.dpr.util.Globals
import kotlin.math.pow
import kotlin.math.sqrt

data class Location(var x: Int, var y: Int) {
    constructor(onTeamA: Boolean): this(
        (1..4).random() * (if (onTeamA) -1 else 1),
        (-2..2).random()
    )

    fun distance(otherLocation: Location): Double {
        return sqrt( (otherLocation.x - x).toDouble().pow(2.0) +
                (otherLocation.y - y).toDouble().pow(2.0))
    }

    fun getOneOff(): List<Location> {
        return listOf(
            Location(x-1, y-1), Location(x, y-1), Location(x+1, y-1),
            Location(x-1, y),                     Location(x+1, y),
            Location(x-1, y+1), Location(x, y+1), Location(x+1, y+1),
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Location

        if (x != other.x) return false
        if (y != other.y) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        return result
    }
}

class CombatantWithStatus(
    val combatant: Combatant,
    val onTeamA: Boolean,
    val turn: Int = 0,
    var location: Location = Location(onTeamA),
    var currentHP: Int = combatant.getHP(),
) : Combatant by combatant, TargetEffect(turn) {

    val deathSavingThrows = mutableListOf<Boolean>()
    val spellCastList = mutableListOf<SpellCast>()

    var target: CombatantWithStatus? = null

    fun distance(target: CombatantWithStatus): Double {
        return distance(target.location)
    }

    fun distance(otherLocation: Location): Double {
        return otherLocation.distance(location)
    }

    fun getPreferredCombatDistance(): Int {
        val hasBetterDexThanStr = getAbilityModifier(AbilityType.Dexterity) >= getAbilityModifier(AbilityType.Strength)
        if (hasBetterDexThanStr) {
            return kotlin.math.min(60, getWeaponList().maxOf { it.range }) // we don't want to be too far from our own group ...
        }
        return Constants.MELEE_RANGE
    }

    fun isDead() = currentHP <= 0 && deathSavingThrows.count { !it } == 3

    fun isDying() = currentHP <= 0 && deathSavingThrows.count { !it } < 3

    fun canTakeAction() = currentHP > 0 && !noActionOrBA

    fun isSlotAvailable(spell: Spell): Boolean {
        val level = spell.properties.Level
        if (level == 0) return true // cantrip

        if (spellCastList.isEmpty()) return true

        if (combatant is PlayerCharacter && spell.name == HuntersMark.getNameWithWS()) {
            val maxSlots = levelToFavoredEnemyMap[combatant.getLevel()] ?: return false
            val slotsUsed = spellCastList.count { it.spell.name == spell.name }
            return (slotsUsed < maxSlots)
        }

        val maxSlots = combatant.getSpellSlots()[level - 1]
        val slotsUsed = spellCastList.count { it.spell.properties.Level == spell.properties.Level && it.spell.name != HuntersMark.getNameWithWS() }
        if (slotsUsed >= maxSlots) Globals.debug("not enough slots: level=$level, slotsUsed=$slotsUsed, max=$maxSlots, spellsUsed = $spellCastList")
        return (slotsUsed < maxSlots)
    }
}