package com.vikinghelmet.dnd.dpr

import com.vikinghelmet.dnd.dpr.scenario.combat.*
import com.vikinghelmet.dnd.dpr.spells.Spell
import com.vikinghelmet.dnd.dpr.util.Constants
import com.vikinghelmet.dnd.dpr.util.Globals
import dev.shivathapaa.logger.api.LoggerFactory
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

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

        val combat = Combat(0,listOf(TestUtil.oleg), listOf(Globals.getMonster("Young Green Dragon")))
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

        assertTrue(breathAttack.action is Spell)

        val spellAttackList = breathAttack.action.getSpellAttacks(0)
        assertEquals(1, spellAttackList.size)

        val spellAttack = spellAttackList[0]
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

        val combat = Combat(0,listOf(TestUtil.leif), listOf(Globals.getMonster("Young Green Dragon")))
        val leif = combat.teamA[0]
        val dragon = combat.teamB[0]

        leif.location = Location(-4, 0)
        dragon.location = Location(3, -2)

        assertEquals(Distance.fromFeet(60), leif.getPreferredCombatDistance())
        assertEquals(Distance.fromFeet(Constants.MELEE_RANGE), dragon.getPreferredCombatDistance())

        // println("leif.actionsAvailable = ${leif.getActionsAvailable()}")

        for (turn in 0..4) {
            combat.turnId = turn
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

            // note: chooseTurnActions does not update "waitingForRecharge" (that happens elsewhere),
            // so in this test the dragon always chooses Poison Breath

            println ("turn=$turn, dragonAttacks: $dragonAttacks")
            assertEquals(listOf("Poison Breath"), dragonAttacks.map { it.action.getActionName() }.toList())
        }
    }


    @Test
    fun chooseTargetLeif() {
        TestUtil.dependency()

        val combat = Combat(0,listOf(TestUtil.leif), listOf(Globals.getMonster("Young Green Dragon")))
        val leif = combat.teamA[0]
        val dragon = combat.teamB[0]

        for(turnId in 0..4) {
            combat.turnId = turnId
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
        val combat = Combat(0,listOf(TestUtil.leif), listOf(Globals.getMonster("Goblin")))
        val preferred = combat.teamA[0].getPreferredTurn(combat.teamB[0], 60)

        assertEquals(listOf("Entangle"), preferred!!.attacks.map { it.action.getActionName() }.toList())
    }

    @Test
    fun getPreferredTurnVersusDragon() {
        TestUtil.dependency()
        val combat = Combat(0,listOf(TestUtil.leif), listOf(Globals.getMonster("Young Green Dragon")))
        val preferred = combat.teamA[0].getPreferredTurn(combat.teamB[0], 60)

        // dragon has high strength, so Entangle is excluded

        assertEquals(listOf("Longbow","Hunter's Mark"), preferred!!.attacks.map { it.action.getActionName() }.toList())
    }

    @Test
    fun getDragonPreferredVersusLeif() {
        TestUtil.dependency()
        val combat = Combat(0,listOf(Globals.getMonster("Young Green Dragon")), listOf(TestUtil.leif))

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
        val combat = Combat(0,listOf(Globals.getMonster("Young Green Dragon")), listOf(Globals.getMonster("Skeleton")))

        // skeleton is immune to poison, so we take the next best option
        var preferred = combat.teamA[0].getPreferredTurn(combat.teamB[0], 0)
        assertEquals(listOf("Multiattack","Bite","Claw","Claw"), preferred!!.attacks.map { it.action.getActionName() }.toList())
    }

    @Test
    fun groundChase() {
        TestUtil.dependency()

        val combat = Combat(0,listOf(TestUtil.leif), listOf(Globals.getMonster("Young Green Dragon")))
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

        val combat = Combat(0,listOf(TestUtil.leif), listOf(Globals.getMonster("Young Green Dragon")), flightSupported = true)
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

    @Test
    fun chooseNewTargetDamagedFriend() {
        TestUtil.dependency()
        val goblin = Globals.getMonster("Goblin")
        val combat = Combat(0,
            listOf(TestUtil.leif, TestUtil.oleg),
            listOf(goblin.copy(), goblin.copy(), goblin.copy()))

        val leif = combat.teamA[0]
        val oleg = combat.teamA[1]
        val heavyHitter = combat.teamB[1]

        // make location/distance a non-issue
        (combat.teamA + combat.teamB).forEach { it.location = Location(0, 0) }

        applyDamage(combat, oleg, heavyHitter, 10)

        val leifTarget = TargetSelector(combat, leif, combat.teamB).select()
        // println(leifTarget)
        assertEquals(heavyHitter, leifTarget.first)
        assertEquals(TargetSelectionStrategy.targetAttackingFriendWhoIsAlmostDead, leifTarget.second)
    }

    private fun applyDamage(combat: Combat, goodGuy: CombatantWithStatus, badGuy: CombatantWithStatus, damage: Int) {
        val attack = badGuy.getPreferredTurn(goodGuy, 0)!!.attacks[0]
        goodGuy.currentHP -= damage
        badGuy.target = goodGuy
        combat.attackResultList.add(CombatAttackResult (badGuy, listOf(goodGuy), damage, attack))
    }

    @Test
    fun chooseNewTargetHurtMePersonally() {
        TestUtil.dependency()
        val goblin = Globals.getMonster("Goblin")
        val combat = Combat(0,
            listOf(TestUtil.leif, TestUtil.oleg),
            listOf(goblin.copy(), goblin.copy(), goblin.copy()))

        val leif = combat.teamA[0]
        val oleg = combat.teamA[1]
        val heavyHitter = combat.teamB[1]

        applyDamage(combat, leif, heavyHitter, 10)

        val leifTarget = TargetSelector(combat, leif, combat.teamB).select()
        // println(leifTarget)
        assertEquals(TargetSelectionStrategy.targetWithHighDamageToAttacker, leifTarget.second)
        assertEquals(heavyHitter, leifTarget.first)
    }

    @Test
    fun chooseNewTargetHurtParty() {
        TestUtil.dependency()
        val goblin = Globals.getMonster("Goblin")
        val combat = Combat(0,
            listOf(TestUtil.eldir, TestUtil.leif, TestUtil.oleg, TestUtil.kael, TestUtil.lars, TestUtil.rhogar),
            listOf(goblin.copy(), goblin.copy(), goblin.copy()))

        val leif = combat.teamA[1]
        val heavyHitter = combat.teamB[1]

        combat.teamA.forEach {
            applyDamage(combat, it, heavyHitter, 2)
        }

        val leifTarget = TargetSelector(combat, leif, combat.teamB).select()
        // println(leifTarget)
        assertEquals(TargetSelectionStrategy.targetWithHighDamageToTeam, leifTarget.second)
        assertEquals(heavyHitter, leifTarget.first)
    }

    @Test
    fun chooseNewTargetNotDead() {
        TestUtil.dependency()
        //Globals.initLogger(LogLevel.DEBUG) // DEBUG

        val dragonNoStatus = Globals.getMonster("Young Green Dragon")
        val combat = Combat(0,
            listOf(TestUtil.eldir, TestUtil.kael, TestUtil.lars, TestUtil.leif, TestUtil.oleg, TestUtil.rhogar),
            listOf(dragonNoStatus))
        /*
            teamA: [
                (Eldir, loc=(-1, 2), dead),
                (Kael, loc=(-28, -23), dead),
                (Lars, loc=(-2, -2), dead),
                (Leif, loc=(-3, 0), dead),
                (Oleg, loc=(-17, -14), dead,
                (Rhogar, loc=(-40, -35), hp=11/16)
            ],
            teamB: [(YoungGreenDragon, loc=(-18, -15), hp=62/136)]
         */

        for (i in 0..4) {
            combat.teamA[i].currentHP = -1
            combat.teamA[i].deathSavingThrows.addAll(listOf(false, false, false))
        }
        val rhogar = combat.teamA[5]
        rhogar.currentHP = 11

        combat.teamA[0].location = Location(-1,2)
        combat.teamA[1].location = Location(-28,-23)
        combat.teamA[2].location = Location(-2,-2)
        combat.teamA[3].location = Location(-3,0)
        combat.teamA[4].location = Location(-17,-14)
        combat.teamA[5].location = Location(-40,35)

        val dragon = combat.teamB[0]
        dragon.currentHP = 62
        dragon.location = Location(-18,-15)

        val target = combat.chooseTarget(dragon)
        println(target)
        assertEquals(rhogar, target)
    }

    @Test
    fun dragonPoisonBreath() {
        TestUtil.dependency()
        // Globals.initLogger(LogLevel.DEBUG) // DEBUG

        val combat = Combat(0,listOf(Globals.getMonster("Young Green Dragon")), listOf(TestUtil.leif, TestUtil.lars, TestUtil.rhogar, TestUtil.kael))

        val dragon = combat.teamA[0]

        // force right cone
        dragon.location = Location(0,0)

        for (i in 0..2) {
            combat.teamB[i].location = Location(i+3, 0)
        }

        combat.teamB[3].location = Location(50,50) // far, far away

        var dragonAttacks = combat.chooseTurnActions(dragon, combat.teamB[0])
        // println("dragonAttacks = $dragonAttacks")
        assertEquals(1, dragonAttacks.size)

        val breathAttack = dragonAttacks[0]
        assertEquals("Poison Breath", breathAttack.action.getActionName())

        val attackResultList = combat.attackWithSpell(dragon, breathAttack.target as CombatantWithStatus, breathAttack)
        // println("attackResultList = $attackResultList")

        assertEquals(1, attackResultList.size)
        assertEquals(3, attackResultList[0].targetList.size)
    }

    @Test
    fun dragonMultiattack() {
        TestUtil.dependency()
        val combat = Combat(0,listOf(Globals.getMonster("Young Green Dragon")), listOf(Globals.getMonster("Skeleton")))

        // force melee
        combat.teamA[0].location = combat.teamB[0].location.copy()

        val dragon = combat.teamA[0]
        val skeleton = combat.teamB[0]

        // skeleton is immune to poison, next best option is Multiattack
//        combat.takeTurn(dragon)
        var dragonAttacks = combat.chooseTurnActions(dragon, skeleton)

        assertEquals(listOf("Multiattack","Bite","Claw","Claw"), dragonAttacks.map { it.action.getActionName() }.toList())
    }

    @Test
    fun cones() {
        TestUtil.dependency()

        for (dir in Direction.entries) {
            val cone = Cone(Location(0,0), dir, 6)
            println("# dir=$dir")
            cone.dump()
            println()
        }
    }
}