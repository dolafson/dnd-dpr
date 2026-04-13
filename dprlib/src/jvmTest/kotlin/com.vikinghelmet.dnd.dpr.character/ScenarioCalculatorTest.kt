package com.vikinghelmet.dnd.dpr.character

import com.vikinghelmet.dnd.dpr.scenario.ScenarioBuilder
import com.vikinghelmet.dnd.dpr.scenario.ScenarioCalculator
import com.vikinghelmet.dnd.dpr.scenario.ScenarioResult
import com.vikinghelmet.dnd.dpr.util.Constants
import com.vikinghelmet.dnd.dpr.util.Constants.DEFAULT_NUM_TARGETS
import com.vikinghelmet.dnd.dpr.util.Constants.DEFAULT_TARGET_RADIUS
import com.vikinghelmet.dnd.dpr.util.Globals
import kotlin.test.Test
import kotlin.test.assertEquals

class ScenarioCalculatorTest {

    @Test
    fun scenarioCalculatorBestResultMeleeOneTurn() {
        val scenarioList = ScenarioBuilder(TestUtil.leif, Globals.getMonster("Goblin"))
            .build(Constants.MELEE_RANGE, 1, DEFAULT_NUM_TARGETS, DEFAULT_TARGET_RADIUS)

        val scenarioResultList = scenarioList.map { ScenarioCalculator(it).calculateDPRForAllTurns() }.toList()
        val topResult = ScenarioResult.topResults(scenarioResultList, 1)[0]

        assertEquals(listOf("Shortsword","Shortsword"), topResult.scenario.turns[0].attacks.map { it.getLabel() } )
    }

    @Test
    fun scenarioCalculatorBestResultRangeOneTurn() {
        val scenarioList = ScenarioBuilder(TestUtil.leif, Globals.getMonster("Goblin"))
            .build(Constants.MELEE_RANGE*2, 1, DEFAULT_NUM_TARGETS, DEFAULT_TARGET_RADIUS)

        val scenarioResultList = scenarioList.map { ScenarioCalculator(it).calculateDPRForAllTurns() }.toList()
        val topResult = ScenarioResult.topResults(scenarioResultList, 1)[0]

        assertEquals(listOf("Longbow","Hail of Thorns"), topResult.scenario.turns[0].attacks.map { it.getLabel() } )
    }
    
    @Test
    fun scenarioCalculatorBestResultMeleeTwoTurns() {
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
    fun scenarioCalculatorBestResultRangeTwoTurns() {
        val scenarioList = ScenarioBuilder(TestUtil.leif, Globals.getMonster("Goblin"))
            .build(Constants.MELEE_RANGE*2, 2, DEFAULT_NUM_TARGETS, DEFAULT_TARGET_RADIUS)

        val scenarioResultList = scenarioList.map { ScenarioCalculator(it).calculateDPRForAllTurns() }.toList()
        val topResult = ScenarioResult.topResults(scenarioResultList, 1)[0]

        assertEquals(listOf("Longbow","Hail of Thorns"), topResult.scenario.turns[0].attacks.map { it.getLabel() } )
        assertEquals(listOf("Longbow","Hail of Thorns"), topResult.scenario.turns[1].attacks.map { it.getLabel() } )
    }

    @Test
    fun scenarioCalculatorBestResultRangeThreeTurns() {
        val scenarioList = ScenarioBuilder(TestUtil.leif, Globals.getMonster("Goblin"))
            .build(Constants.MELEE_RANGE*2, 3, DEFAULT_NUM_TARGETS, DEFAULT_TARGET_RADIUS)

        val scenarioResultList = scenarioList.map { ScenarioCalculator(it).calculateDPRForAllTurns() }.toList()
        val topResult = ScenarioResult.topResults(scenarioResultList, 1)[0]

        assertEquals(listOf("Longbow","Hunter's Mark"), topResult.scenario.turns[0].attacks.map { it.getLabel() } )
        assertEquals(listOf("Longbow","Hail of Thorns"), topResult.scenario.turns[1].attacks.map { it.getLabel() } )
        assertEquals(listOf("Longbow","Hail of Thorns"), topResult.scenario.turns[2].attacks.map { it.getLabel() } )
    }
}