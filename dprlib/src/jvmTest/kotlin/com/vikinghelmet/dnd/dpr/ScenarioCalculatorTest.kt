package com.vikinghelmet.dnd.dpr

import com.vikinghelmet.dnd.dpr.scenario.Scenario
import com.vikinghelmet.dnd.dpr.scenario.ScenarioBuilder
import com.vikinghelmet.dnd.dpr.scenario.ScenarioCalculator
import com.vikinghelmet.dnd.dpr.scenario.ScenarioResult
import com.vikinghelmet.dnd.dpr.turn.Attack
import com.vikinghelmet.dnd.dpr.turn.Turn
import com.vikinghelmet.dnd.dpr.util.Constants.DEFAULT_NUM_TARGETS
import com.vikinghelmet.dnd.dpr.util.Constants.DEFAULT_TARGET_RADIUS
import com.vikinghelmet.dnd.dpr.util.Constants.MELEE_RANGE
import com.vikinghelmet.dnd.dpr.util.Globals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ScenarioCalculatorTest {

    fun bestDPR(numTurns: Int, range: Int): SimpleResult {
        val scenarioList = ScenarioBuilder(TestUtil.leif, Globals.getMonster("Goblin"))
            .build(range, numTurns, DEFAULT_NUM_TARGETS, DEFAULT_TARGET_RADIUS)
        val scenarioResultList = scenarioList.map { ScenarioCalculator(it).calculateDPRForAllTurns() }.toList()
        return SimpleResult (ScenarioResult.topResults(scenarioResultList, 1)[0])
    }

    @Test
    fun maxDPROneTurnMelee() {
        assertEquals (SimpleResult(7, listOf(listOf("Shortsword","Shortsword"))), bestDPR(1, MELEE_RANGE))
    }

    @Test
    fun maxDPROneTurnRange() {
        assertEquals (SimpleResult(10, listOf(listOf("Longbow","Hail of Thorns"))), bestDPR(1, MELEE_RANGE*2))
    }
    
    @Test
    fun maxDPRTwoTurnMelee() {
        assertEquals (SimpleResult(19, listOf(listOf("Shortsword","Hunter's Mark"), listOf("Shortsword","Shortsword"))),
            bestDPR(2, MELEE_RANGE))
    }

    @Test
    fun maxDPRTwoTurnRange() {
        assertEquals (SimpleResult(20, List(2) { listOf("Longbow","Hail of Thorns") }), bestDPR(2, MELEE_RANGE*2))
    }

    @Test
    fun maxDPRThreeTurnMelee() {
        assertEquals (SimpleResult(33, listOf(listOf("Shortsword","Hunter's Mark")) + List(2) { listOf("Shortsword","Shortsword") }),
            bestDPR(3, MELEE_RANGE))
    }

    @Test
    fun maxDPRThreeTurnRange() {
        assertEquals (SimpleResult(31, listOf(listOf("Longbow","Hunter's Mark")) + List(2) { listOf("Longbow","Hail of Thorns") }),
            bestDPR(3, MELEE_RANGE*2))
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

        println("noSleepResults.totalDamage   = ${ noSleepResults.totalDamage }")
        println("withSleepResults.totalDamage = ${ withSleepResults.totalDamage }")

        assertEquals(3.575f, noSleepResults.attackResults[1].damagePerRound.final)
        assertEquals(8.448001f, withSleepResults.attackResults[1].damagePerRound.final)

        // 5 firebolts with no sleep is roughly 1/2 the damage of 4 firebolts with sleep
        assertEquals(17.875f, noSleepResults.totalDamage)
        assertEquals(33.792004f, withSleepResults.totalDamage)
    }

}