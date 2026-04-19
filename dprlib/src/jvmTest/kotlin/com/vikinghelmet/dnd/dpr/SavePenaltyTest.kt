package com.vikinghelmet.dnd.dpr

import com.vikinghelmet.dnd.dpr.TestUtil.wwCSPlan
import com.vikinghelmet.dnd.dpr.character.actions.ActionModifier
import com.vikinghelmet.dnd.dpr.scenario.*
import com.vikinghelmet.dnd.dpr.turn.ActionCalculator
import com.vikinghelmet.dnd.dpr.turn.Attack
import com.vikinghelmet.dnd.dpr.turn.AvgMinMax
import com.vikinghelmet.dnd.dpr.turn.Turn
import com.vikinghelmet.dnd.dpr.util.Constants.DEFAULT_NUM_TARGETS
import com.vikinghelmet.dnd.dpr.util.Constants.DEFAULT_TARGET_RADIUS
import com.vikinghelmet.dnd.dpr.util.Constants.MELEE_RANGE
import com.vikinghelmet.dnd.dpr.util.Globals
import com.vikinghelmet.dnd.dpr.util.TargetEffect
import dev.shivathapaa.logger.api.LoggerFactory
import kotlinx.serialization.Transient
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

//@EnabledIfSystemProperty(named = "RunSlowTests", matches = "true")
class SavePenaltyTest {
    @Transient private val logger = LoggerFactory.get(SavePenaltyTest::class.simpleName ?: "")

    @Test
    fun wwCsMelee() {
        val character = wwCSPlan
        val expectedList = TestUtil.getExpectedResults("wwCsMelee")

        for (level in listOf(5, 8)) {
            val best = TestUtil.bestDPR(level, wwCSPlan, MELEE_RANGE)
            println("$level: $best")
        }

        for (level in listOf(5, 8)) {
            character.editableFields.level = level
            val scenarioList = ScenarioBuilder(character, Globals.getMonster("Goblin"))
                .build(MELEE_RANGE, 5, DEFAULT_NUM_TARGETS, DEFAULT_TARGET_RADIUS)

            val scenarioResultList = scenarioList.map { ScenarioCalculator(it).calculateDPRForAllTurns() }.toList()
            val topResult = ScenarioResult.topResults(scenarioResultList, 1)[0]

            val expected = expectedList.find { it.level == level }
            assertEquals(expected, SimpleResult(topResult))

            assertEquals(AvgMinMax(0.7f, 0.49f, 0.91f, 0.7f),   topResult.attackResults[0].chanceToHit, "sword1, t=0, level=$level")
            assertEquals(AvgMinMax(0.7f, 0.49f, 0.91f, 0.85f),  topResult.attackResults[1].chanceToHit, "sword2, t=0, level=$level")
            assertEquals(AvgMinMax(1f,1f,1f,1f),                topResult.attackResults[2].chanceToHit, "HM, t=0, level=$level")

            //ssertEquals(AvgMinMax(0f,0f,0f,0.85f),             topResult.attackResults[3].chanceToHit, "sword1, t=1, level=$level")
            assertEquals(0.85f,  Globals.round2(topResult.attackResults[3].chanceToHit.final), "sword1, t=1, level=$level")

            assertEquals (8f,     Globals.round2(topResult.attackResults[0].damagePerRound.final), "d01, level=$level")
            assertEquals (7.49f,  Globals.round2(topResult.attackResults[1].damagePerRound.final), "d02, level=$level")
            assertEquals (0f,     topResult.attackResults[2].damagePerRound.final,                     "d03, level=$level")
            assertEquals (13.07f, Globals.round2(topResult.attackResults[3].damagePerRound.final), "d11, level=$level")
        }
    }

    @Test
    fun savePenaltyNoImpactOnWeaponAttackViaScenarioCalculator() {
        val character = wwCSPlan
        val monster = Globals.getMonster("Goblin")
        val weapon = character.getWeapon("Shortsword")

        val polarAttack  = Attack (monster, weapon, actionModifiers = mutableListOf(ActionModifier.PolarStrikes))
        val huntersMark  = Attack (monster, Globals.getSpell("Hunter's Mark", character.is2014()))
        val weaponAttack = Attack (monster, weapon)
        val bonusAttack  = Attack (monster, weapon, isBonusAction = true)
        val turnList     = listOf(Turn(listOf(polarAttack, weaponAttack, huntersMark))) +
                            List(3) { Turn(listOf(polarAttack, weaponAttack, bonusAttack)) }

        val scenario = Scenario(character, turnList, DEFAULT_NUM_TARGETS, DEFAULT_TARGET_RADIUS)

        //Globals.initLogger(LogLevel.DEBUG)
        for(level in listOf(5, 8)) {
            character.editableFields.level = level
            var result = ScenarioCalculator(scenario).calculateDPRForAllTurns()

            assertEquals(AvgMinMax(0.7f, 0.49f, 0.91f, 0.7f),   result.attackResults[0].chanceToHit, "sword1, t=0, level=$level")
            assertEquals(AvgMinMax(0.7f, 0.49f, 0.91f, 0.85f),  result.attackResults[1].chanceToHit, "sword2, t=0, level=$level")
            assertEquals(AvgMinMax(1f,1f,1f,1f),                result.attackResults[2].chanceToHit, "HM, t=0, level=$level")

            //assertEquals(AvgMinMax(0.7f, 0.49f, 0.91f, 0.85f),  result.attackResults[3].chanceToHit, "sword1, t=1, level=$level")
            assertEquals(0.85f,  Globals.round2(result.attackResults[3].chanceToHit.final), "sword1, t=1, level=$level")

            assertEquals(AvgMinMax(8.0f, 5.4f, 10.59f, 8.0f),   result.attackResults[0].damagePerRound, "dpr sword1, t=0, level=$level")
            assertEquals(AvgMinMax(6.12f, 4.17f, 8.08f, 7.49f), result.attackResults[1].damagePerRound, "dpr sword2, t=0, level=$level")
            assertEquals(AvgMinMax(0f,0f,0f,0f),                result.attackResults[2].damagePerRound, "dpr HM, t=0, level=$level")

            //assertEquals(AvgMinMax(10.62f, 7.13f, 14.12f, 13.07f), result.attackResults[3].damagePerRound, "dpr sword1, t=1, level=$level")
            assertEquals(13.07f,  Globals.round2(result.attackResults[3].damagePerRound.final), "sword1, t=1, level=$level")
        }
    }

    @Test
    fun savePenaltyNoImpactOnWeaponAttackViaActionCalculator() {
        val character = wwCSPlan
        character.editableFields.level = 7

        val weapon  = character.getWeapon("Shortsword")
        val monster = Globals.getMonster("Goblin")

        val weaponAttack = Attack (monster, weapon)
        val singleTurn   = listOf(Turn(listOf(weaponAttack)))
        val scenario     = Scenario(character, singleTurn, DEFAULT_NUM_TARGETS, DEFAULT_TARGET_RADIUS)

        // first, calculate results with no savePenalty
        var noPenaltyResult = ActionCalculator(scenario, EffectManager(ArrayList()))
            .getMeleeOrRangeDPR(weapon, weaponAttack, 1, 1, 1)

        assertEquals (AvgMinMax(0.7f,  0.49f, 0.91f, 0.7f),  noPenaltyResult.chanceToHit, "noPenalty %hit")
        assertEquals (AvgMinMax(6.12f, 4.17f, 8.08f, 6.12f), noPenaltyResult.damagePerRound, "noPenalty dpr")

        // second, results for same scenario WITH savePenalty
        val effectManager = EffectManager(mutableListOf(
            TargetEffect(startTurn = 0, probability = 0.7f, savePenalty = mutableListOf("1d4"))))

        var withPenaltyResult = ActionCalculator(scenario, effectManager)
            .getMeleeOrRangeDPR(weapon, weaponAttack, 1, 1, 1)

        assertEquals (AvgMinMax(0.7f,  0.49f, 0.91f, 0.7f),  withPenaltyResult.chanceToHit, "withPenalty %hit")
        assertEquals (AvgMinMax(6.12f, 4.17f, 8.08f, 6.12f), withPenaltyResult.damagePerRound, "withPenalty dpr")

        println("effectManager = $effectManager")
    }
}