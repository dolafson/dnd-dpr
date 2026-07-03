package com.vikinghelmet.dnd.dpr

import com.vikinghelmet.dnd.dpr.action.Attack
import com.vikinghelmet.dnd.dpr.action.Turn
import com.vikinghelmet.dnd.dpr.action.enums.DamageType
import com.vikinghelmet.dnd.dpr.scenario.TargetEffect
import com.vikinghelmet.dnd.dpr.scenario.combat.*
import com.vikinghelmet.dnd.dpr.scenario.combat.location.Cone
import com.vikinghelmet.dnd.dpr.scenario.combat.location.Direction
import com.vikinghelmet.dnd.dpr.scenario.combat.location.Distance
import com.vikinghelmet.dnd.dpr.scenario.combat.location.Location
import com.vikinghelmet.dnd.dpr.scenario.combat.results.CombatActionResult
import com.vikinghelmet.dnd.dpr.scenario.combat.results.DamageResult
import com.vikinghelmet.dnd.dpr.scenario.combat.save.SavingThrowGenerator
import com.vikinghelmet.dnd.dpr.spells.Spell
import com.vikinghelmet.dnd.dpr.spells.SpellsWithComplexRules
import com.vikinghelmet.dnd.dpr.util.AttackAdvantage
import com.vikinghelmet.dnd.dpr.util.Condition
import com.vikinghelmet.dnd.dpr.util.Constants
import com.vikinghelmet.dnd.dpr.util.Globals
import dev.shivathapaa.logger.api.LoggerFactory
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
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
        assertEquals(Location(2, -1), loc1) // movement occurred

        loc2.moveTowardLocation(loc1, 6)
        assertEquals(Location(3, -2), loc2) // movement occurred

        loc1.moveTowardLocation(loc2, 6)
        assertEquals(Location(2, -1), loc1) // no movement needed

        loc2.moveTowardLocation(loc1, 6)
        assertEquals(Location(3, -2), loc2)  // no movement needed
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
                0,1 -> assertEquals(5, possibleTurns.size) // LB, LB+Hail, LB+HM, Entangle, MS
                2,3,4 -> assertEquals(2, possibleTurns.size) // LB, MS
            }

            for (turn in possibleTurns) {
                var spellAttack = turn.attacks.firstNotNullOfOrNull { it.action as? Spell }
                if (spellAttack != null) {
                    println("spellAttack = $spellAttack")
                    leif.recordSpellCasting(spellAttack, turnId)
                    break
                }
            }
        }
    }

    @Test
    fun getPreferredTurnVersusGoblin() {
        TestUtil.dependency()
        val combat = Combat(0,listOf(TestUtil.leif), listOf(Globals.getMonster("Goblin")))
        val preferred = combat.teamA[0].getPreferredTurn(ActionGoal.Attack, combat.teamB[0], 60)

        assertEquals(TurnOptionRanking.SpellThatGivesAdvantage, preferred!!.second)
        assertEquals(listOf("Entangle"), preferred.first.attacks.map { it.action.getActionName() }.toList())
    }

    @Test
    fun getPreferredTurnVersusDragon() {
        TestUtil.dependency()
        val combat = Combat(0,listOf(TestUtil.leif), listOf(Globals.getMonster("Young Green Dragon")))
        val preferred = combat.teamA[0].getPreferredTurn(ActionGoal.Attack, combat.teamB[0], 60)

        // dragon has high strength, so Entangle is excluded

        assertEquals(listOf("Longbow","Hunter's Mark"), preferred!!.first.attacks.map { it.action.getActionName() }.toList())
    }

    @Test
    fun getDragonPreferredVersusLeif() {
        TestUtil.dependency()
        val combat = Combat(0,listOf(Globals.getMonster("Young Green Dragon")), listOf(TestUtil.leif))

        // dragon has no attack options when range = 60
        var preferred = combat.teamA[0].getPreferredTurn(ActionGoal.Attack,combat.teamB[0], 60)
        assertNull(preferred)

        // dragon has lots of options at range = 0
        preferred = combat.teamA[0].getPreferredTurn(ActionGoal.Attack,combat.teamB[0], 0)
        assertEquals(listOf("Poison Breath"), preferred!!.first.attacks.map { it.action.getActionName() }.toList())
    }

    @Test
    fun getDragonPreferredVersusUndead() {
        TestUtil.dependency()
        val combat = Combat(0,listOf(Globals.getMonster("Young Green Dragon")), listOf(Globals.getMonster("Skeleton")))

        // skeleton is immune to poison, so we take the next best option
        var preferred = combat.teamA[0].getPreferredTurn(ActionGoal.Attack, combat.teamB[0], 0)
        assertEquals(listOf("Multiattack","Bite","Claw","Claw"), preferred!!.first.attacks.map { it.action.getActionName() }.toList())
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
            distance = leif.moveAwayFromTarget(listOf(dragon), distance, combat)
            leif.logMovement("moving away from targets", oldLoc, distance)

            when (turnId) {
                0 -> { assertEquals(Location(-9, -8), leif.location); assertEquals(65, distance.toFeet()) }
                1 -> { assertEquals(Location(-15, -14), leif.location); assertEquals(60, distance.toFeet()) }
                2 -> { assertEquals(Location(-21, -20), leif.location); assertEquals(50, distance.toFeet()) }
            }

            oldLoc = dragon.location.copy()
            distance = dragon.moveTowardTarget(leif, combat)
            dragon.logMovement("moving toward target $leif", oldLoc, distance)

            when (turnId) {
                0 -> { assertEquals(Location(-5, -7), dragon.location); assertEquals(20, distance.toFeet()) }
                1 -> { assertEquals(Location(-13, -13), dragon.location); assertEquals(10, distance.toFeet()) }
                2 -> { assertEquals(Location(-20, -19), dragon.location); assertEquals(5, distance.toFeet()) }
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
            distance = leif.moveAwayFromTarget(listOf(dragon), distance, combat)
            leif.logMovement("moving away from targets", oldLoc, distance)

            when (turnId) {
                0 -> { assertEquals(Location(-20, -8), leif.location); assertEquals(170, distance.toFeet()) }
                1 -> { assertEquals(Location(-26, -14), leif.location); assertEquals(125, distance.toFeet()) }
                2 -> { assertEquals(Location(-32, -20), leif.location); assertEquals(75, distance.toFeet()) }
            }

            oldLoc = dragon.location.copy()
            distance = dragon.moveTowardTarget(leif, combat)
            dragon.logMovement("moving toward target $leif", oldLoc, distance)

            when (turnId) {
                0 -> { assertEquals(Location(-2, -7), dragon.location); assertEquals(90, distance.toFeet()) }
                1 -> { assertEquals(Location(-18, -13), dragon.location); assertEquals(40, distance.toFeet()) }
                2 -> { assertEquals(Location(-31, -19), dragon.location); assertEquals(5, distance.toFeet()) }
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
        val attack = badGuy.getPreferredTurn(ActionGoal.Attack, goodGuy, 0)!!.first.attacks[0]
        goodGuy.currentHP -= damage
        badGuy.target = goodGuy

        val damageList = listOf(DamageResult(damage, DamageType.undefined))
        combat.actionResultList.add(CombatActionResult(badGuy, goodGuy, 0,0,0, attack, damageList))
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
            combat.teamA[i].die(0)
        }
        val rhogar = combat.teamA[5]
        rhogar.currentHP = 11

        combat.teamA[0].location = Location(-1, 2)
        combat.teamA[1].location = Location(-28, -23)
        combat.teamA[2].location = Location(-2, -2)
        combat.teamA[3].location = Location(-3, 0)
        combat.teamA[4].location = Location(-17, -14)
        combat.teamA[5].location = Location(-40, 35)

        val dragon = combat.teamB[0]
        dragon.currentHP = 62
        dragon.location = Location(-18, -15)

        val target = CombatTurn(combat, dragon).chooseAttackTarget()
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
        dragon.location = Location(0, 0)

        for (i in 0..2) {
            combat.teamB[i].location = Location(i + 3, 0)
        }

        combat.teamB[3].location = Location(50, 50) // far, far away

        val combatTurn = CombatTurn(combat, dragon, target = combat.teamB[0])

        var dragonAttacks = combatTurn.chooseTurnActions(ActionGoal.Attack).attacks
        // println("dragonAttacks = $dragonAttacks")
        assertEquals(1, dragonAttacks.size)

        val breathAttack = dragonAttacks[0]
        assertEquals("Poison Breath", breathAttack.action.getActionName())

        val attackResultList = combatTurn.attackWithSpell(breathAttack)
        // println("attackResultList = $attackResultList")

        assertEquals(3, attackResultList.size)
    }


    @Test
    fun dragonPoisonBreathVeryClose() {
        TestUtil.dependency()
        // Globals.initLogger(LogLevel.DEBUG) // DEBUG

        val combat = Combat(0,listOf(Globals.getMonster("Young Green Dragon")), listOf(TestUtil.oleg))

        val dragon = combat.teamA[0]
        val oleg   = combat.teamB[0]

        dragon.location = Location(3, 0)
        oleg.location   = Location(2, -1) // very close

        val combatTurn = CombatTurn(combat, dragon, target = oleg)
        var dragonAttacks = combatTurn.chooseTurnActions(ActionGoal.Attack).attacks

        // println("dragonAttacks = $dragonAttacks")
        assertEquals(1, dragonAttacks.size)

        val breathAttack = dragonAttacks[0]
        assertEquals("Poison Breath", breathAttack.action.getActionName())

        val cones = Cone.getIntersection(dragon.location, oleg.location, 6)
        val coneNames = cones.map { it.direction.toString() }.toList()

        assertEquals(listOf("downLeft","downLeft2"), coneNames)

        val attackResultList = combatTurn.attackWithSpell(breathAttack)
        // println("attackResultList = $attackResultList")

        assertEquals(1, attackResultList.size)
    }

    @Test
    fun dragonMultiattack() {
        TestUtil.dependency()
        val combat = Combat(0,listOf(Globals.getMonster("Young Green Dragon")), listOf(Globals.getMonster("Skeleton")))

        // force melee
        combat.teamA[0].location = Location(0,0)
        combat.teamB[0].location = Location(1,0)

        val dragon = combat.teamA[0]
        val skeleton = combat.teamB[0]

        // skeleton is immune to poison, next best option is Multiattack
        // this validates attack filtering in CombatantWithStatus.isSpellValid (getDamageImmunities)
        val combatTurn = CombatTurn(combat, dragon, target = skeleton)
        var dragonAttacks = combatTurn.chooseTurnActions(ActionGoal.Attack).attacks

        assertEquals(listOf("Multiattack","Bite","Claw","Claw"), dragonAttacks.map { it.action.getActionName() }.toList())
    }

    @Test
    fun cones() {
        TestUtil.dependency()

        val points = mutableListOf<Location>()

        val center = Location(0, 0)
        for (dir in Direction.entries) {
            val cone = Cone(center, dir, 6)
            println("# dir=$dir")
            cone.dump()
            points.addAll (cone.getPoints())
        }

        println("# dir=ALL")
        Cone.dump(center, 6, points)
    }

    @Test
    fun entangleDuration() {
        TestUtil.dependency()
//        var goblinWithHighAC = Globals.getMonster("Goblin").copy(
//            armor_class = mutableListOf(ArmorClass(type = "unobtanium", value=50)) // increase AC to keep goblin alive longer
//        )
//        val combat = Combat(0, listOf(TestUtil.leif), listOf(goblinWithHighAC))
        val combat = Combat(0, listOf(TestUtil.leif), listOf(Globals.getMonster("Goblin")))
        val leif = combat.teamA[0]
        val goblin = combat.teamB[0]

        // force the goblin to fail its saving throw (entangle has a STR save)
        val autoFailEffect = TargetEffect(0, autoFailStrAndDexSaves = true)
        goblin.add(autoFailEffect)

        // this test expects leif to cast Entangle - see also getPreferredTurnVersusGoblin()
        val preferredTurnOption = leif.getPreferredTurn(ActionGoal.Attack, goblin, 10, combat)
        assertEquals(listOf("Entangle"), preferredTurnOption!!.first.attacks.map { it.action.getActionName() }.toList())

        CombatTurn(combat, leif).fullTurn()

        // https://www.dndbeyond.com/spells/2085-entangle?srsltid=AfmBOoqr7BbKDxX_vWM2WEXKy8YxQaMmDT7ptb4dS4y9CoXZpRwjkHHd
        assertTrue (goblin.any { it.conditions.contains(Condition.Restrained) })
        assertEquals (AttackAdvantage.advantage, goblin.getAttacksAgainstMe())
        assertEquals (AttackAdvantage.disadvantage, goblin.getAttacksAgainstOthers())
        // TODO: getDisadvantageOnSave -> DEX

        for (turnId in 1..15) {
            // spell duration is checked at the beginning of the spell casters turn
            leif.checkForSaveAtStartOfTurn (turnId)

            if (turnId >= 10) {
                assertEquals(0, goblin.toList().filter { it != autoFailEffect }.size)
                assertEquals (AttackAdvantage.normal, goblin.getAttacksAgainstMe())
                assertEquals (AttackAdvantage.normal, goblin.getAttacksAgainstOthers())
            }
            else {
                assertEquals(1, goblin.toList().filter { it != autoFailEffect }.size)
                assertEquals (AttackAdvantage.advantage, goblin.getAttacksAgainstMe())
                assertEquals (AttackAdvantage.disadvantage, goblin.getAttacksAgainstOthers())
            }
        }
    }

    @Test
    fun entangleExitEarly() {
        TestUtil.dependency()

        // results are non-deterministic, so take an average across multiple samples
        val turnCountList = mutableListOf<Int>()

        repeat(100) {
            val combat = Combat(0, listOf(TestUtil.getCharacter("party2/leif.json")), listOf(Globals.getMonster("Goblin").copy()))
            //val combat = Combat(0, listOf(TestUtil.leif), listOf(Globals.getMonster("Goblin")))
            val leif = combat.teamA[0]
            val goblin = combat.teamB[0]

            val preferredTurnOption = leif.getPreferredTurn(ActionGoal.Attack, goblin, 10, combat)
            assertEquals(listOf("Entangle"), preferredTurnOption!!.first.attacks.map { it.action.getActionName() }.toList())

            CombatTurn(combat, leif).fullTurn()

            // ignore any turns where spell failed; we want to measure the avg duration after the spell is in effect

            if (goblin.toList().isNotEmpty()) {
                // println("goblin is restrained")
                for (turnId in 1..15) {
                    // spell duration is checked at the beginning of the spell casters turn
                    leif.checkForSaveAtStartOfTurn(turnId)

                    // target gets a chance to save by taking an action on their turn
                    CombatTurn(combat, goblin).fullTurn()

                    if (goblin.toList().isEmpty()) {
                        //println("condition ended on turn=$turnId")
                        turnCountList.add(turnId)
                        break
                    }
                }
            }
        }

        val avg = turnCountList.average().toInt()
        assertTrue(avg in 2..<5)
    }

    @Test
    fun getClericPreferredTurnVersusGoblin() {
        TestUtil.dependency()
        val combat = Combat(0,listOf(TestUtil.kael), listOf(Globals.getMonster("Goblin")))

        val kael = combat.teamA[0]
        // force melee range
        kael.location = combat.teamB[0].location.copy()

        println("getActionsAvailable = ${ kael.getActionsAvailable()}")

        for (range in listOf(5,60)) {

            val sorted = kael.getRankedTurnOptions(ActionGoal.Attack, combat.teamB[0], range, combat)

            sorted.forEach {
                println("ranking=${it.ranking}, action=${it.turn.attacks[0].action.getActionName()} ")
            }

            var preferred = kael.getPreferredTurn(ActionGoal.Attack, combat.teamB[0], range)
            val spellName = preferred!!.first.attacks.map { it.action.getActionName() }.toList()[0]
            println("range=$range, spellName=$spellName")
            when (range) {
                5 -> {
                    //assertEquals(TurnOptionRanking.SpellWithDeathPrevention, preferred.second)
                    //assertEquals("Spare the Dying", spellName)
                    assertEquals(SpellsWithComplexRules.Bane, SpellsWithComplexRules.fromName(spellName)) // TODO: inflict wounds ?
                    assertEquals(TurnOptionRanking.SpellThatGivesAdvantage, preferred.second)
                }
                60 -> {
                    assertEquals(TurnOptionRanking.SpellThatGivesAdvantage, preferred.second)
                    assertEquals("Guiding Bolt", spellName)
                }
            }
        }
    }


    @Test
    fun getClericPreferredWhenDamaged() {
        TestUtil.dependency()
        val combat = Combat(0,listOf(TestUtil.kael), listOf(Globals.getMonster("Goblin")))

        // force melee range
        combat.teamA[0].location = combat.teamB[0].location.copy()

        combat.teamA[0].currentHP = 3

        println("getActionsAvailable = ${ combat.teamA[0].getActionsAvailable()}")

        for (range in listOf(5,60)) {
            var preferred = combat.teamA[0].getPreferredTurn(ActionGoal.Heal, combat.teamB[0], range)
            val spellName = preferred!!.first.attacks.map { it.action.getActionName() }.toList()[0]
            when (range) {
                5 -> {
                    //assertEquals(TurnOptionRanking.SpellWithDeathPrevention, preferred.second)
                    //assertEquals("Spare the Dying", spellName)
                    assertEquals(SpellsWithComplexRules.ChannelDivinityPreserveLife, SpellsWithComplexRules.fromName(spellName))
                    assertEquals(TurnOptionRanking.SpellWithRestoreHPAOE, preferred.second)
                }
                60 -> {
                    assertEquals(TurnOptionRanking.SpellWithRestoreHP, preferred.second)
                    assertEquals("Healing Word", spellName)
                }
            }
        }
    }

    @Test
    fun breakConcentration() {
        TestUtil.dependency()
        val combat = Combat(0, listOf(TestUtil.leif), listOf(Globals.getMonster("Goblin")))
        val leif = combat.teamA[0]
        val goblin = combat.teamB[0]

        // force the goblin to fail its saving throw (entangle has a STR save)
        val autoFailEffect = TargetEffect(0, autoFailStrAndDexSaves = true)
        goblin.add(autoFailEffect)

        // this test expects leif to cast Entangle - see also getPreferredTurnVersusGoblin()
        val preferredTurnOption = leif.getPreferredTurn(ActionGoal.Attack, goblin, 10, combat)
        assertEquals(listOf("Entangle"), preferredTurnOption!!.first.attacks.map { it.action.getActionName() }.toList())

        CombatTurn(combat, leif).fullTurn()

        assertTrue (goblin.any { it.conditions.contains(Condition.Restrained) })
        assertEquals(1, leif.spellCastList.filter {it.isStillRunning()}.count())

        for (turnId in 1..3) {
            // spell duration is checked at the beginning of the spell casters turn
            leif.checkForSaveAtStartOfTurn (turnId)

            assertTrue (goblin.any { it.conditions.contains(Condition.Restrained) })
            assertEquals(1, leif.spellCastList.filter {it.isStillRunning()}.count())
        }

        // make sure Leif succeeds on next two saves, but fails the 3rd
        val generator = mockk<SavingThrowGenerator>()
        every { generator.makeSavingThrow(any(), any()) } returnsMany listOf(true, true, false)
        leif.savingThrowGenerator = generator

        for (turnId in 4..6) {
            leif.applyDamage(4, listOf(DamageResult(3,DamageType.piercing))) // leif takes damage each turn, triggering a concentration check

            when (turnId) {
                4,5 -> {
                    assertTrue (goblin.any { it.conditions.contains(Condition.Restrained) })
                    assertEquals(1, leif.spellCastList.filter {it.isStillRunning()}.count())
                }
                6 -> {
                    assertFalse (goblin.any { it.conditions.contains(Condition.Restrained) })
                    assertEquals (0, leif.spellCastList.filter {it.isStillRunning()}.count())
                }
            }
        }
    }


    @Test
    fun getTurnOptionRankingList() {
        TestUtil.dependency()
        val combat = Combat(0,listOf(TestUtil.leif), listOf(Globals.getMonster("Goblin")))
        val leif = combat.teamA[0]
        val goblin = combat.teamB[0]
        val range = 60

        val available = leif.getActionsAvailable()
        available.getRanges().forEach { r ->
            println("available[$r] = ${available.mapOfLists[r]}")
        }

        val possible = leif.getPossibleTurns(goblin, range)
        possible.forEach { println("possibleTurn = $it") }

        val sorted = combat.teamA[0].getRankedTurnOptions(ActionGoal.Attack, goblin, range, combat)
        sorted.forEach { println("turnOptionRanking = $it") }

 //       assertEquals(TurnOptionRanking.SpellThatGivesAdvantage, preferred!!.second)
 //       assertEquals(listOf("Entangle"), preferred.first.attacks.map { it.action.getActionName() }.toList())
    }

    @Test
    fun getClericHealing() {
        TestUtil.dependency()
        val combat = Combat(0,listOf(TestUtil.kael), listOf(Globals.getMonster("Goblin")))

        // force melee range
        combat.teamA[0].location = Location(0, 0)
        combat.teamB[0].location = Location(1, 0)

        val kael = combat.teamA[0]
        kael.currentHP = 3 // force lowHP

        println("before turn, kael.currentHp = ${kael.currentHP}")

        while (kael.currentHP < kael.getHP()) {
            CombatTurn(combat, kael).fullTurn()
            assertTrue(kael.currentHP > 3)
        }

        assertEquals(kael.currentHP, kael.getHP())
    }

    @Test
    fun initiativeDups() {
        TestUtil.dependency()
        //Globals.initLogger(LogLevel.DEBUG) // DEBUG

        val dragonNoStatus = Globals.getMonster("Young Green Dragon")
        val combat = Combat(0,
            listOf(TestUtil.eldir, TestUtil.kael, TestUtil.lars, TestUtil.leif, TestUtil.oleg, TestUtil.rhogar),
            listOf(dragonNoStatus))

        val teamA = combat.teamA
        val teamB = combat.teamB

        for (i in 0..3) {
            teamA[i].initiative = 10
        }
        val initiativeList = (teamA + teamB).sortedByDescending { it.initiative }.toList()

        println("")
        println("initiative list: $initiativeList")
        println("")
        initiativeList.forEach { println("${it.initiative}: $it") }
        println("")
    }



    @Test
    fun dragonPoisonBreathRecharge() {
        TestUtil.dependency()
        // Globals.initLogger(LogLevel.DEBUG) // DEBUG

        val combat = Combat(0,listOf(Globals.getMonster("Young Green Dragon")), listOf(TestUtil.leif))

        val dragon = combat.teamA[0]
        val leif = combat.teamB[0]
        // force right cone
        dragon.location = Location(0, 0)
        leif.location = Location(1, 0)

        var dragonAttackResult = CombatTurn(combat, dragon).fullTurn()
        assertEquals(1, dragonAttackResult.size)
        //println("dragonAttackResult 1 = $dragonAttackResult")

        assertEquals("Poison Breath", dragonAttackResult[0].actionTaken)
        assertEquals(1, dragon.waitingForRecharge.size)

        CombatTurn(combat, dragon).fullTurn()
        //println("dragonAttackResult 2 = $dragonAttackResult")

        // recharge happens 2 times out of 6; should be guaranteed to happen at least once after 10 rounds
        for (turn in 1..10) {
            dragon.checkForSaveAtStartOfTurn(turn)
        }
        assertEquals(0, dragon.waitingForRecharge.size)
    }

    @Test
    fun prayerOfHealing() {
        TestUtil.dependency()
        val combat = Combat(0,listOf(TestUtil.kael3), listOf(Globals.getMonster("Goblin")))

        // TODO - there are multiple things broken with this spell
        // 1. it routinely returns 0 healing (probably due to #2 below ...)
        // 2. it shows up with 2024 data (while kael is 2014)
        // 3. due to a long casting time (10 mins), it should ONLY be used AFTER combat

        // force melee range
        combat.teamA[0].location = combat.teamB[0].location.copy()

        val kael = combat.teamA[0]
        val goblin = combat.teamB[0]
        kael.currentHP = 3

        val available = kael.getActionsAvailable()
        println("getActionsAvailable = ${ available}")

        val meleeRangeList = available.mapOfLists[Constants.MELEE_RANGE]!!

        meleeRangeList.filter { it is Spell }.forEach { it as Spell
            println("is2014=${it.is2014()}, $it")
        }

        val prayer = meleeRangeList.first { it.getActionName() == "Prayer of Healing" } as Spell
        assertNotNull(prayer)

        println("casting time = ${prayer.getCastingTime()}")

        assertFalse (kael.isSpellViable (goblin, combat, Turn(listOf(Attack(goblin, prayer)))))
    }

    @Test
    fun spareTheDying() {
        val input = "Spare the Dying"
        val words = input.split(Regex("[\\s_]+"))
        val joined = words.joinToString("") { word -> word.lowercase().replaceFirstChar { it.uppercase() } }
        assertEquals("SpareTheDying", joined)
    }

    @Test
    fun cureWounds() {
        TestUtil.dependency()
        val combat = Combat(0,listOf(TestUtil.kael, TestUtil.leif), listOf(Globals.getMonster("Goblin")))

        // force melee range
        combat.teamA[0].location = Location(-1, 0)
        combat.teamA[1].location = Location(0, 0)
        combat.teamB[0].location = Location(1, 0)

        val kael = combat.teamA[0]
        val leif = combat.teamA[1]

        kael.applyDamage(0, listOf(DamageResult(kael.getHP()+1, DamageType.fire)))

        val goal = leif.getActionGoal(combat)
        assertEquals(ActionGoal.Heal, goal)

        val turn = leif.getPreferredTurn(goal, kael, Constants.MELEE_RANGE, combat)
        println("turn = $turn")

        val spell =  turn!!.first.getSpell()!!
        assertEquals("Cure Wounds", spell.name)

        val healAmount = leif.getHealingAmount(spell, true)
        println("healAmount = $healAmount")
        assertTrue(healAmount > 0)
    }
}