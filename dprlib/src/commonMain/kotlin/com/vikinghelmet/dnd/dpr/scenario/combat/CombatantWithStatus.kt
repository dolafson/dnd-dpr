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

class CombatantWithStatus(
    val combatant: Combatant,
    val newName: String,
    val onTeamA: Boolean,
    val turn: Int = 0,
    var location: Location = Location(onTeamA),
    var currentHP: Int = combatant.getHP(),
    var currentInitiative: Int = (1..20).random() + combatant.getInitiativeBonus(),

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

    fun deathSave() {
        val saveRoll = (1..20).random()
        if (saveRoll == 1) {
            deathSavingThrows.add(false)
            deathSavingThrows.add(false)
        }
        else if (saveRoll < 10) {
            deathSavingThrows.add(false)
        }
        else if (saveRoll < 20) {
            deathSavingThrows.add(true)
        }

        if (saveRoll == 20 || (deathSavingThrows.filter { it == true }.count() >= 3)) {
            deathSavingThrows.clear()
            currentHP = 1
        }
    }

    // TODO: move most of the moveAwayFromTarget() method into Location class
    fun moveAwayFromTarget(targetList: List<CombatantWithStatus>, closestDistanceStart: Double) {
        var closestDistance = closestDistanceStart
        val maxMoves = getWalkingSpeed() / 5
        var loc = location
        val targetLocationList = targetList.map { it.location }.toList()

        for (i in 1..maxMoves) {
            for (oneOffLoc in loc.getOneOff()) {
                // for the given new location, find the closest target
                val nextClosest = targetLocationList.minByOrNull { it.distance(oneOffLoc) }
                val nextClosestDistance = nextClosest!!.distance(oneOffLoc)

                // if the new "closest" is larger than before, that's progress: take it
                if (closestDistance < nextClosestDistance) {
                    closestDistance = nextClosestDistance
                    loc = oneOffLoc
                    break
                }
            }
            if (location == loc) break
            location = loc
        }
    }

    fun moveTowardTarget(target: CombatantWithStatus) {
        location.moveTowardLocation(target.location, getWalkingSpeed() / 5)
    }

    fun getPreferredCombatDistance(): Int {
        val hasBetterDexThanStr = getAbilityModifier(AbilityType.Dexterity) >= getAbilityModifier(AbilityType.Strength)
        if (hasBetterDexThanStr) {
            return kotlin.math.min(60, getWeaponList().maxOf { it.range }) // we don't want to be too far from our own group ...
        }
        return Constants.MELEE_RANGE
    }

    fun shortName() = newName // getName().replace(" .*".toRegex(), "")

    fun summary(): String {
        val buffer = StringBuilder("(").append(shortName())
        if (isDead()) {
            buffer.append(", dead")
        }
        else if (isDying()) {
            val failCount = deathSavingThrows.filter { !it }.count()
            val passCount = deathSavingThrows.filter { it }.count()
            buffer.append(", dying, saves=-$failCount/+$passCount")
        }
        else {
            buffer.append(", hp=$currentHP/${getHP()}")
        }
        return buffer.append(")").toString()
    }

    fun isDeadOrDying() = currentHP <= 0
    fun isDead() = currentHP <= 0 && deathSavingThrows.filter { !it }.count() >= 3

    fun isDying() = currentHP <= 0 && deathSavingThrows.filter { !it }.count() < 3

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

    fun toFullString(): String {
        return "CombatantWithStatus(combatant=$combatant, onTeamA=$onTeamA, turn=$turn, location=$location, currentHP=$currentHP, deathSavingThrows=$deathSavingThrows, spellCastList=$spellCastList, target=$target)"
    }

    // override fun toString() = getName()
    override fun toString() = shortName()
}