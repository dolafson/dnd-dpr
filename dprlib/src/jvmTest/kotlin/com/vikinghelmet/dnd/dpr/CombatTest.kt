package com.vikinghelmet.dnd.dpr

import com.vikinghelmet.dnd.dpr.scenario.combat.Combat
import com.vikinghelmet.dnd.dpr.scenario.combat.Distance
import com.vikinghelmet.dnd.dpr.scenario.combat.Location
import com.vikinghelmet.dnd.dpr.scenario.combat.SpellCast
import com.vikinghelmet.dnd.dpr.spells.Spell
import com.vikinghelmet.dnd.dpr.util.Constants
import com.vikinghelmet.dnd.dpr.util.Globals
import dev.shivathapaa.logger.api.LoggerFactory
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

private val logger = LoggerFactory.get(CombatTest::class.simpleName ?: "")

class CombatTest {
    @Test
    fun moveToward() {
        TestUtil.dependency()
        //Globals.initLogger(LogLevel.DEBUG) // DEBUG

        val loc1 = Location(-4, 0)
        val loc2 = Location(3, -2)

        // note: a lot of movement may occur on a diagonal

        loc1.moveTowardLocation(loc2, 6)
        assertEquals(Location(2,-1), loc1) // movement occurred

        loc2.moveTowardLocation(loc1, 6)
        assertEquals(Location(3,-2), loc2) // movement occurred

        loc1.moveTowardLocation(loc2, 6)
        assertEquals(Location(2,-1), loc1) // no movement needed

        loc2.moveTowardLocation(loc1, 6)
        assertEquals(Location(3,-2), loc2)  // no movement needed
    }

    @Test
    fun chooseTargetOleg() {
        TestUtil.dependency()

        val combat = Combat(listOf(TestUtil.oleg), listOf(Globals.getMonster("Young Green Dragon")))
        val oleg = combat.teamA[0]
        val dragon = combat.teamB[0]

        oleg.location = Location(-4, 0)
        dragon.location = Location(3, -2)

        assertEquals(Distance.fromFeet(Constants.MELEE_RANGE), oleg.getPreferredCombatDistance())
        assertEquals(Distance.fromFeet(Constants.MELEE_RANGE), dragon.getPreferredCombatDistance())

        combat.chooseTarget(oleg)
        assertEquals(Location(2,-1), oleg.location) // movement occurred

        println("oleg.actionsAvailable = ${oleg.getActionsAvailable()}")

        var olegAttacks = combat.chooseTurnActions(oleg, dragon)
        println ("olegAttacks: $olegAttacks")

        combat.chooseTarget(dragon)
        assertEquals(Location(3,-2), dragon.location) // movement occurred

        var dragonAttacks = combat.chooseTurnActions(dragon, oleg)
        println ("dragonAttacks: $dragonAttacks")

        // second round

        println("oleg.actionsAvailable = ${oleg.getActionsAvailable()}")
        combat.chooseTarget(oleg)
        assertEquals(Location(2,-1), oleg.location) // movement occurred

        olegAttacks = combat.chooseTurnActions(oleg, dragon)
        println ("olegAttacks: $olegAttacks")

        combat.chooseTarget(dragon)
        assertEquals(Location(3,-2), dragon.location) // movement occurred

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


    @Test
    fun chooseTargetLeifOld() {
        TestUtil.dependency()

        val combat = Combat(listOf(TestUtil.leif), listOf(Globals.getMonster("Young Green Dragon")))
        val leif = combat.teamA[0]
        val dragon = combat.teamB[0]

        leif.location = Location(-4, 0)
        dragon.location = Location(3, -2)

        assertEquals(Distance.fromFeet(60), leif.getPreferredCombatDistance())
        assertEquals(Distance.fromFeet(Constants.MELEE_RANGE), dragon.getPreferredCombatDistance())

        // println("leif.actionsAvailable = ${leif.getActionsAvailable()}")

        for (turn in 0..4) {
            combat.turn = turn
            combat.chooseTarget(leif)
            var leifAttacks = combat.chooseTurnActions(leif, dragon)
            println ("turn=$turn, leifAttacks: $leifAttacks")
            when (turn) {
                0 -> assertEquals(listOf("Longbow","Hunter's Mark"), leifAttacks.map { it.action.getActionName() }.toList())
                1,2 -> assertEquals(listOf("Longbow","Hail of Thorns"), leifAttacks.map { it.action.getActionName() }.toList())
                3,4 -> assertEquals(listOf("Dagger","Shortsword"), leifAttacks.map { it.action.getActionName() }.toList())
            }

            combat.chooseTarget(dragon)
            var dragonAttacks = combat.chooseTurnActions(dragon, leif)

            when(turn) {
                0, 1  -> assertTrue(dragonAttacks.isEmpty())
                2,3,4 -> assertEquals(listOf("Poison Breath"), dragonAttacks.map { it.action.getActionName() }.toList())
            }
            println ("turn=$turn, dragonAttacks: $dragonAttacks")
        }
    }


    @Test
    fun chooseTargetLeif() {
        TestUtil.dependency()

        val combat = Combat(listOf(TestUtil.leif), listOf(Globals.getMonster("Young Green Dragon")))
        val leif = combat.teamA[0]
        val dragon = combat.teamB[0]

        for(turnId in 0..4) {
            combat.turn = turnId
            var possibleTurns = leif.getPossibleTurns(dragon, 60)

            for (turn in possibleTurns) println("turnId=$turnId, possibleTurn = $turn")

            //var attackNames = possibleTurns.map { it.attacks.map { it2 -> it2.action.getActionName() } }.flatten()

            when (turnId) {
//                0,1 -> assertEquals(listOf("Longbow","Hunter's Mark"), attackNames)
                0,1 -> assertEquals(5, possibleTurns.size) // LB, LB+Hail, LB+HM, Entangle, MS
                2   -> assertEquals(3, possibleTurns.size) // LB, LB+HM, MS
                3,4 -> assertEquals(2, possibleTurns.size) // LB, MS
            }

            for (turn in possibleTurns) {
                var firstSpellAction = turn.attacks.firstNotNullOfOrNull { it.action as? Spell }
                if (firstSpellAction != null) {
                    println("firstSpellAction = $firstSpellAction")
                    leif.spellCastList.add(SpellCast(leif, firstSpellAction!!, 0))
                    break
                }
            }
        }
    }

    @Test
    fun getPreferredTurnVersusGoblin() {
        TestUtil.dependency()
        val combat = Combat(listOf(TestUtil.leif), listOf(Globals.getMonster("Goblin")))
        val preferred = combat.teamA[0].getPreferredTurn(combat.teamB[0], 60)

        assertEquals(listOf("Entangle"), preferred!!.attacks.map { it.action.getActionName() }.toList())
    }

    @Test
    fun getPreferredTurnVersusDragon() {
        TestUtil.dependency()
        val combat = Combat(listOf(TestUtil.leif), listOf(Globals.getMonster("Young Green Dragon")))
        val preferred = combat.teamA[0].getPreferredTurn(combat.teamB[0], 60)

        // dragon has high strength, so Entangle is excluded

        assertEquals(listOf("Longbow","Hunter's Mark"), preferred!!.attacks.map { it.action.getActionName() }.toList())
    }

    @Test
    fun getDragonPreferredVersusLeif() {
        TestUtil.dependency()
        val combat = Combat(listOf(Globals.getMonster("Young Green Dragon")), listOf(TestUtil.leif))

        // dragon has no attack options when range = 60
        var preferred = combat.teamA[0].getPreferredTurn(combat.teamB[0], 60)
        assertNull(preferred)

        // dragon has lots of options at range = 0
        preferred = combat.teamA[0].getPreferredTurn(combat.teamB[0], 0)
        assertEquals(listOf("Poison Breath"), preferred!!.attacks.map { it.action.getActionName() }.toList())
    }

    @Test
    fun getDragonPreferredVersusUndead() {
        TestUtil.dependency()
        val combat = Combat(listOf(Globals.getMonster("Young Green Dragon")), listOf(Globals.getMonster("Skeleton")))

        // skeleton is immune to poison, so we take the next best option
        var preferred = combat.teamA[0].getPreferredTurn(combat.teamB[0], 0)
        assertEquals(listOf("Multiattack"), preferred!!.attacks.map { it.action.getActionName() }.toList())
    }

    @Test
    fun groundChase() {
        TestUtil.dependency()

        val combat = Combat(listOf(TestUtil.leif), listOf(Globals.getMonster("Young Green Dragon")))
        val leif = combat.teamA[0]
        val dragon = combat.teamB[0]

        // 13:09:30  INFO | Combat - turn=0, teamA: [(Leif, loc=(-3, -2), hp=16/16)], teamB: [(YoungGreenDragon, loc=(3, -2), hp=136/136)]

        leif.location = Location(-3, -2)
        dragon.location = Location(3, -2)

        var oldLoc: Location? = null
        var distance: Distance = leif.distance(dragon)

        for (turnId in 0..2) {
            println("########## turn=$turnId")
            oldLoc = leif.location.copy()
            distance = leif.moveAwayFromTarget(listOf(dragon), distance)
            leif.logMovement("moving away from targets", oldLoc, distance)

            when (turnId) {
                0 -> { assertEquals(Location(-9,-8), leif.location); assertEquals(65, distance.toFeet()) }
                1 -> { assertEquals(Location(-15,-14), leif.location); assertEquals(60, distance.toFeet()) }
                2 -> { assertEquals(Location(-21,-20), leif.location); assertEquals(50, distance.toFeet()) }
            }

            oldLoc = dragon.location.copy()
            distance = dragon.moveTowardTarget(leif)
            dragon.logMovement("moving toward target $leif", oldLoc, distance)

            when (turnId) {
                0 -> { assertEquals(Location(-5,-7), dragon.location); assertEquals(20, distance.toFeet()) }
                1 -> { assertEquals(Location(-13,-13), dragon.location); assertEquals(10, distance.toFeet()) }
                2 -> { assertEquals(Location(-20,-19), dragon.location); assertEquals(5, distance.toFeet()) }
            }

            if (distance.toFeet() <= Constants.MELEE_RANGE) {
                println("melee range, stop moving")
                break
            }
        }
    }

    @Test
    fun airChase() {
        TestUtil.dependency()

        val combat = Combat(listOf(TestUtil.leif), listOf(Globals.getMonster("Young Green Dragon")), flightSupported = true)
        val leif = combat.teamA[0]
        val dragon = combat.teamB[0]
        
        // for flight test, move them further apart (140 ft)
        leif.location = Location(-14, -2)
        dragon.location = Location(14, -2)

        var oldLoc: Location? = null
        var distance: Distance = leif.distance(dragon)

        for (turnId in 0..5) {
            println("########## turn=$turnId")
            oldLoc = leif.location.copy()
            distance = leif.moveAwayFromTarget(listOf(dragon), distance)
            leif.logMovement("moving away from targets", oldLoc, distance)

            when (turnId) {
                0 -> { assertEquals(Location(-20,-8), leif.location); assertEquals(170, distance.toFeet()) }
                1 -> { assertEquals(Location(-26,-14), leif.location); assertEquals(125, distance.toFeet()) }
                2 -> { assertEquals(Location(-32,-20), leif.location); assertEquals(75, distance.toFeet()) }
            }

            oldLoc = dragon.location.copy()
            distance = dragon.moveTowardTarget(leif)
            dragon.logMovement("moving toward target $leif", oldLoc, distance)

            when (turnId) {
                0 -> { assertEquals(Location(-2,-7), dragon.location); assertEquals(90, distance.toFeet()) }
                1 -> { assertEquals(Location(-18,-13), dragon.location); assertEquals(40, distance.toFeet()) }
                2 -> { assertEquals(Location(-31,-19), dragon.location); assertEquals(5, distance.toFeet()) }
            }

            if (distance.toFeet() <= Constants.MELEE_RANGE) {
                println("melee range, stop moving")
                break
            }
        }
    }
}