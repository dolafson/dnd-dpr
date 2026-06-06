package com.vikinghelmet.dnd.dpr

import com.vikinghelmet.dnd.dpr.scenario.combat.Combat
import com.vikinghelmet.dnd.dpr.scenario.combat.Distance
import com.vikinghelmet.dnd.dpr.scenario.combat.Location
import com.vikinghelmet.dnd.dpr.spells.Spell
import com.vikinghelmet.dnd.dpr.util.Constants
import com.vikinghelmet.dnd.dpr.util.Globals
import dev.shivathapaa.logger.api.LogLevel
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class CombatTest {
    @Test
    fun moveToward() {
        TestUtil.dependency()
        //Globals.initLogger(LogLevel.DEBUG) // DEBUG

        val loc1 = Location(-4, 0)
        val loc2 = Location(3, -2)

        loc1.moveTowardLocation(loc2, 6)
        assertEquals(Location(2,0), loc1) // movement occurred

        loc2.moveTowardLocation(loc1, 6)
        assertEquals(Location(3,-1), loc2) // movement occurred

        loc1.moveTowardLocation(loc2, 6)
        assertEquals(Location(2,0), loc1) // no movement needed

        loc2.moveTowardLocation(loc1, 6)
        assertEquals(Location(3,-1), loc2)  // no movement needed
    }

    @Test
    fun chooseTarget() {
        TestUtil.dependency()
        Globals.initLogger(LogLevel.INFO)
        //Globals.initLogger(LogLevel.DEBUG)

        val combat = Combat(listOf(TestUtil.oleg), listOf(Globals.getMonster("Young Green Dragon")))
        val oleg = combat.teamA[0]
        val dragon = combat.teamB[0]

        oleg.location = Location(-4, 0)
        dragon.location = Location(3, -2)

        assertEquals(Distance.fromFeet(Constants.MELEE_RANGE), oleg.getPreferredCombatDistance())
        assertEquals(Distance.fromFeet(Constants.MELEE_RANGE), dragon.getPreferredCombatDistance())

        combat.chooseTarget(oleg)
        assertEquals(Location(2,0), oleg.location) // movement occurred

        println("oleg.actionsAvailable = ${oleg.getActionsAvailable()}")

        var olegAttacks = combat.chooseTurnActions(oleg, dragon)
        println ("olegAttacks: $olegAttacks")

        combat.chooseTarget(dragon)
        assertEquals(Location(3,-1), dragon.location) // movement occurred

        var dragonAttacks = combat.chooseTurnActions(dragon, oleg)
        println ("dragonAttacks: $dragonAttacks")

        // second round

        println("oleg.actionsAvailable = ${oleg.getActionsAvailable()}")
        combat.chooseTarget(oleg)
        assertEquals(Location(2,0), oleg.location) // movement occurred

        olegAttacks = combat.chooseTurnActions(oleg, dragon)
        println ("olegAttacks: $olegAttacks")

        combat.chooseTarget(dragon)
        assertEquals(Location(3,-1), dragon.location) // movement occurred

        dragonAttacks = combat.chooseTurnActions(dragon, oleg)
        println ("dragonAttacks: $dragonAttacks")

        val breathAttack = dragonAttacks[0]
        println ("breathAttack: $breathAttack")

        val spellAttack = (breathAttack.action as Spell).getSpellAttacks(0)[0]

        var initialDamage = combat.computeDamage(breathAttack, oleg, false, spellAttack.getDamageList())
        println("initial damage = $initialDamage")

        val damageOnFail = combat.applySavingThrowDamageModifiers (spellAttack, breathAttack, initialDamage, false)
        val damageOnSave = combat.applySavingThrowDamageModifiers (spellAttack, breathAttack, initialDamage, true)

        println ("damageOnFail: $damageOnFail")
        println ("damageOnSave: $damageOnSave")

        assertEquals(initialDamage, damageOnFail)
        assertNotEquals(0, damageOnSave)
        assertEquals(damageOnSave*2, damageOnFail)
    }
}