package com.vikinghelmet.dnd.dpr

import com.vikinghelmet.dnd.dpr.TestUtil.wwCSPlan
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
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

//@EnabledIfSystemProperty(named = "RunSlowTests", matches = "true")
class SavePenaltyTest {
    @Transient private val logger = LoggerFactory.get(SavePenaltyTest::class.simpleName ?: "")

    @Test
    fun wwCsMelee() {
        val character = wwCSPlan

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

            assertEquals(AvgMinMax(0.7f, 0.49f, 0.91f, 0.7f),   topResult.attackResults[0].chanceToHit, "sword1, t=0, level=$level")
            assertEquals(AvgMinMax(0.7f, 0.49f, 0.91f, 0.85f),  topResult.attackResults[1].chanceToHit, "sword2, t=0, level=$level")
            assertEquals(AvgMinMax(1f,1f,1f,1f),                topResult.attackResults[2].chanceToHit, "HM, t=0, level=$level")
            assertEquals(AvgMinMax(0f,0f,0f,0.85f),             topResult.attackResults[3].chanceToHit, "sword1, t=1, level=$level")
        }
    }

    @Test
    fun savePenaltyNoImpactOnWeaponAttackViaScenarioCalculator() {
        val character = wwCSPlan

        val weaponAttack = Attack (Globals.getMonster("Goblin"), character.getWeapon("Shortsword"))
        val twoTurns     = listOf(Turn(listOf(weaponAttack)), Turn(listOf(weaponAttack)))
        val scenario     = Scenario(character, twoTurns, 10, DEFAULT_TARGET_RADIUS)

        for(level in listOf(7, 8)) {
            character.editableFields.level = level
            var result = ScenarioCalculator(scenario).calculateDPRForAllTurns()

            assertEquals(0.7f,   result.attackResults[0].chanceToHit.final, "sword1, t=0, level=$level")
            assertEquals(0.847f, result.attackResults[1].chanceToHit.final, "sword2, t=0, level=$level")
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
        val scenario     = Scenario(character, singleTurn, 10, DEFAULT_TARGET_RADIUS)

        // first, calculate results with no savePenalty
        var noPenaltyResult = ActionCalculator(scenario, EffectManager(ArrayList()))
            .getMeleeOrRangeDPR(weapon, weaponAttack, 1, 1, 1)

        Assertions.assertEquals(AvgMinMax(0.7f, 0.49f, 0.91f, 0.7f), noPenaltyResult.chanceToHit, "noPenalty %hit")

        // second, results for same scenario WITH savePenalty
        val effectManager = EffectManager(mutableListOf(
            TargetEffect(startTurn = 0, probability = 0.7f, savePenalty = mutableListOf("1d4"))))

        var withPenaltyResult = ActionCalculator(scenario, effectManager)
            .getMeleeOrRangeDPR(weapon, weaponAttack, 1, 1, 1)

        Assertions.assertEquals(AvgMinMax(0.7f, 0.49f, 0.91f, 0.7f), withPenaltyResult.chanceToHit, "withPenalty %hit")

        println("effectManager = $effectManager")
    }
}