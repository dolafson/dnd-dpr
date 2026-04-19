package com.vikinghelmet.dnd.dpr

import com.vikinghelmet.dnd.dpr.scenario.Scenario
import com.vikinghelmet.dnd.dpr.scenario.ScenarioBuilder
import com.vikinghelmet.dnd.dpr.scenario.ScenarioCalculator
import com.vikinghelmet.dnd.dpr.scenario.ScenarioResult
import com.vikinghelmet.dnd.dpr.turn.Attack
import com.vikinghelmet.dnd.dpr.turn.Turn
import com.vikinghelmet.dnd.dpr.util.Constants
import com.vikinghelmet.dnd.dpr.util.Constants.DEFAULT_NUM_TARGETS
import com.vikinghelmet.dnd.dpr.util.Constants.DEFAULT_TARGET_RADIUS
import com.vikinghelmet.dnd.dpr.util.Globals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ScenarioCalculatorTest {

    @Test
    fun maxDPROneTurnMelee() {
        val scenarioList = ScenarioBuilder(TestUtil.leif, Globals.getMonster("Goblin"))
            .build(Constants.MELEE_RANGE, 1, DEFAULT_NUM_TARGETS, DEFAULT_TARGET_RADIUS)

        val scenarioResultList = scenarioList.map { ScenarioCalculator(it).calculateDPRForAllTurns() }.toList()
        val topResult = ScenarioResult.topResults(scenarioResultList, 1)[0]

        assertEquals(listOf("Shortsword","Shortsword"), topResult.scenario.turns[0].attacks.map { it.getLabel() } )
    }

    @Test
    fun maxDPROneTurnRange() {
        val scenarioList = ScenarioBuilder(TestUtil.leif, Globals.getMonster("Goblin"))
            .build(Constants.MELEE_RANGE*2, 1, DEFAULT_NUM_TARGETS, DEFAULT_TARGET_RADIUS)

        val scenarioResultList = scenarioList.map { ScenarioCalculator(it).calculateDPRForAllTurns() }.toList()
        val topResult = ScenarioResult.topResults(scenarioResultList, 1)[0]

        assertEquals(listOf("Longbow","Hail of Thorns"), topResult.scenario.turns[0].attacks.map { it.getLabel() } )
    }
    
    @Test
    fun maxDPRTwoTurnMelee() {
        val scenarioList = ScenarioBuilder(TestUtil.leif, Globals.getMonster("Goblin"))
            .build(Constants.MELEE_RANGE, 2, DEFAULT_NUM_TARGETS, DEFAULT_TARGET_RADIUS)

        val scenarioResultList = scenarioList.map { ScenarioCalculator(it).calculateDPRForAllTurns() }.toList()
        val topResult = ScenarioResult.topResults(scenarioResultList, 1)[0]
/*
        ScenarioResult.topResults(scenarioResultList, 5).forEach { r ->
            println("${ Globals.getPercent(r.totalDPR) } \t ${ r.scenario.getLabel() }")
            r.attackResults.forEach { println(it) }
        }
*/
        assertEquals(listOf("Shortsword","Hunter's Mark"),  topResult.scenario.turns[0].attacks.map { it.getLabel() } )
        assertEquals(listOf("Shortsword","Shortsword"), topResult.scenario.turns[1].attacks.map { it.getLabel() } )
    }

    @Test
    fun maxDPRThreeTurnMelee() {
        val scenarioList = ScenarioBuilder(TestUtil.leif, Globals.getMonster("Goblin"))
            .build(Constants.MELEE_RANGE, 3, DEFAULT_NUM_TARGETS, DEFAULT_TARGET_RADIUS)

        val scenarioResultList = scenarioList.map { ScenarioCalculator(it).calculateDPRForAllTurns() }.toList()
        val topResult = ScenarioResult.topResults(scenarioResultList, 1)[0]
        /*
                ScenarioResult.topResults(scenarioResultList, 5).forEach { r ->
                    println("${ Globals.getPercent(r.totalDPR) } \t ${ r.scenario.getLabel() }")
                    r.attackResults.forEach { println(it) }
                }
        */
        assertEquals(listOf("Shortsword","Hunter's Mark"),  topResult.scenario.turns[0].attacks.map { it.getLabel() } )
        assertEquals(listOf("Shortsword","Shortsword"), topResult.scenario.turns[1].attacks.map { it.getLabel() } )
        assertEquals(listOf("Shortsword","Shortsword"), topResult.scenario.turns[2].attacks.map { it.getLabel() } )
    }

    @Test
    fun maxDPRTwoTurnRange() {
        val scenarioList = ScenarioBuilder(TestUtil.leif, Globals.getMonster("Goblin"))
            .build(Constants.MELEE_RANGE*2, 2, DEFAULT_NUM_TARGETS, DEFAULT_TARGET_RADIUS)

        val scenarioResultList = scenarioList.map { ScenarioCalculator(it).calculateDPRForAllTurns() }.toList()
        val topResult = ScenarioResult.topResults(scenarioResultList, 1)[0]

        assertEquals(listOf("Longbow","Hail of Thorns"), topResult.scenario.turns[0].attacks.map { it.getLabel() } )
        assertEquals(listOf("Longbow","Hail of Thorns"), topResult.scenario.turns[1].attacks.map { it.getLabel() } )
    }

    @Test
    fun maxDPRThreeTurnRange() {
        val scenarioList = ScenarioBuilder(TestUtil.leif, Globals.getMonster("Goblin"))
            .build(Constants.MELEE_RANGE*2, 3, DEFAULT_NUM_TARGETS, DEFAULT_TARGET_RADIUS)

        val scenarioResultList = scenarioList.map { ScenarioCalculator(it).calculateDPRForAllTurns() }.toList()
        val topResult = ScenarioResult.topResults(scenarioResultList, 1)[0]

        assertEquals(listOf("Longbow","Hunter's Mark"), topResult.scenario.turns[0].attacks.map { it.getLabel() } )
        assertEquals(listOf("Longbow","Hail of Thorns"), topResult.scenario.turns[1].attacks.map { it.getLabel() } )
        assertEquals(listOf("Longbow","Hail of Thorns"), topResult.scenario.turns[2].attacks.map { it.getLabel() } )
    }

    @Test
    fun spellConditionCarryForward() {
        val character = TestUtil.eldir
        val sleep    = Globals.getSpell("Sleep", character.is2014())
        val fireBolt = Globals.getSpell("Fire Bolt", character.is2014())
        val monster  = Globals.getMonster("Goblin")

        val fourFirebolts = listOf(
            Turn(listOf(Attack (monster, fireBolt))),
            Turn(listOf(Attack (monster, fireBolt))),
            Turn(listOf(Attack (monster, fireBolt))),
            Turn(listOf(Attack (monster, fireBolt))),
        )
        val fiveFirebolts = fourFirebolts + listOf(Turn(listOf(Attack (monster, fireBolt))))

        // compare two 5-turn scenarios:  5 firebolts -VS- 1 sleep + 4 firebolts

        val noSleepScenario = Scenario(character, fiveFirebolts, 10, DEFAULT_TARGET_RADIUS)
        val noSleepResults  = ScenarioCalculator(noSleepScenario).calculateDPRForAllTurns()

        val sleepTurn = listOf(Turn(listOf(Attack (monster, sleep))))
        val withSleepScenario = Scenario(character, sleepTurn + fourFirebolts, 10, DEFAULT_TARGET_RADIUS)
        val withSleepResults  = ScenarioCalculator(withSleepScenario).calculateDPRForAllTurns()

        println("noSleepResults[1] = ${ noSleepResults.attackResults[1] }")
        println("withSleepResults[1] = ${withSleepResults.attackResults[1] }")

        assertEquals("", noSleepResults.attackResults[1].startEffects)
        assertEquals("70.0% = Incapacitated, Unconscious, Exhaustion", withSleepResults.attackResults[1].startCondition)

        assertEquals("", noSleepResults.attackResults[1].startCondition)
        assertEquals("70.0% = attackerHasAdvantage;attackerAutoCrit;autoFailSave=[Strength, Dexterity];disadvantageOnAttacks;noActionOrBA;", withSleepResults.attackResults[1].startEffects)

        println("noSleepResults.totalDPR   = ${ noSleepResults.totalDPR }")
        println("withSleepResults.totalDPR = ${ withSleepResults.totalDPR }")

        assertEquals(3.575f, noSleepResults.attackResults[1].damagePerRound.final)
        assertEquals(8.448001f, withSleepResults.attackResults[1].damagePerRound.final)

        // 5 firebolts with no sleep is roughly 1/2 the damage of 4 firebolts with sleep
        assertEquals(17.875f, noSleepResults.totalDPR)
        assertEquals(33.792004f, withSleepResults.totalDPR)
    }

}