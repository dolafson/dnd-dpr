package com.vikinghelmet.dnd.dpr

import com.vikinghelmet.dnd.dpr.TestUtil.gsPlan
import com.vikinghelmet.dnd.dpr.TestUtil.hunterPlan
import com.vikinghelmet.dnd.dpr.TestUtil.wwCSPlan
import com.vikinghelmet.dnd.dpr.TestUtil.wwPlan
import com.vikinghelmet.dnd.dpr.character.Character
import com.vikinghelmet.dnd.dpr.scenario.ScenarioBuilder
import com.vikinghelmet.dnd.dpr.scenario.ScenarioCalculator
import com.vikinghelmet.dnd.dpr.scenario.ScenarioResult
import com.vikinghelmet.dnd.dpr.util.Constants
import com.vikinghelmet.dnd.dpr.util.Constants.DEFAULT_NUM_TARGETS
import com.vikinghelmet.dnd.dpr.util.Constants.DEFAULT_TARGET_RADIUS
import com.vikinghelmet.dnd.dpr.util.Globals
import dev.shivathapaa.logger.api.LoggerFactory
import kotlinx.serialization.Transient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfSystemProperty

class DprTest {
    @Transient private val logger = LoggerFactory.get(DprTest::class.simpleName ?: "")

    val allRangerSubclasses = listOf(hunterPlan, gsPlan, wwPlan, wwCSPlan)

    // --------------------------------------------------------------------------
    // MELEE: Hunter

    val hunterBestMelee = listOf(
        listOf("Shortsword", "Hunter's Mark"),
        listOf("Shortsword[ColossusSlayer]", "Shortsword"),
        listOf("Shortsword", "Shortsword"),
        listOf("Shortsword[ColossusSlayer]", "Shortsword"),
        listOf("Shortsword", "Shortsword"),
    )

    val hunterBestMeleeL5 = listOf(
        listOf("Shortsword", "Shortsword", "Hunter's Mark"),
        listOf("Shortsword[ColossusSlayer]", "Shortsword", "Shortsword"),
        listOf("Shortsword", "Shortsword", "Shortsword"),
        listOf("Shortsword[ColossusSlayer]", "Shortsword", "Shortsword"),
        listOf("Shortsword", "Shortsword", "Shortsword"),
    )

    // --------------------------------------------------------------------------
    // MELEE: GloomStalker

    val gsBestMeleeL3 = listOf(
        listOf("Shortsword[DreadfulStrike]", "Hunter's Mark"),
        listOf("Shortsword[DreadfulStrike]", "Shortsword"),
        listOf("Shortsword", "Shortsword"),
        listOf("Shortsword", "Shortsword"),
        listOf("Shortsword", "Shortsword"),
    )

    val gsBestMeleeL4 = gsBestMeleeL3.withIndex().map { (index, value) -> if (index != 2) value else gsBestMeleeL3[1] }

    val gsBestMeleeL5 = listOf(
        listOf("Shortsword[DreadfulStrike]", "Shortsword", "Hunter's Mark"),
        listOf("Shortsword[DreadfulStrike]", "Shortsword", "Shortsword"),
        listOf("Shortsword[DreadfulStrike]", "Shortsword", "Shortsword"),
        listOf("Shortsword", "Shortsword", "Shortsword"),
        listOf("Shortsword", "Shortsword", "Shortsword"),
    )

    val gsBestMeleeL8  = gsBestMeleeL5.withIndex().map { (index, value) -> if (index != 3) value else gsBestMeleeL5[2] }
    val gsBestMeleeL12 = gsBestMeleeL8.withIndex().map { (index, value) -> if (index != 4) value else gsBestMeleeL8[2] }

    // --------------------------------------------------------------------------
    // MELEE: WinterWalker

    val wwBestMelee = listOf(
        listOf("Shortsword[PolarStrikes]", "Hunter's Mark"),
        listOf("Shortsword[PolarStrikes]", "Shortsword"),
        listOf("Shortsword[PolarStrikes]", "Shortsword"),
        listOf("Shortsword[PolarStrikes]", "Shortsword"),
        listOf("Shortsword[PolarStrikes]", "Shortsword"),
    )

    val wwBestMeleeL5 = listOf(
        listOf("Shortsword[PolarStrikes]", "Shortsword", "Hunter's Mark"),
        listOf("Shortsword[PolarStrikes]", "Shortsword", "Shortsword"),
        listOf("Shortsword[PolarStrikes]", "Shortsword", "Shortsword"),
        listOf("Shortsword[PolarStrikes]", "Shortsword", "Shortsword"),
        listOf("Shortsword[PolarStrikes]", "Shortsword", "Shortsword"),
    )


    // --------------------------------------------------------------------------
    // RANGE: Hunter

    val hunterBestRangeL3 = listOf(
        listOf("Longbow", "Hunter's Mark"),
        listOf("Longbow[ColossusSlayer]", "Hail of Thorns"),
        listOf("Longbow", "Hail of Thorns"),
        listOf("Longbow[ColossusSlayer]", "Hail of Thorns"),
        listOf("Longbow"),
    )

    val hunterBestRangeL5 = listOf(
        listOf("Longbow", "Longbow", "Hunter's Mark"),
        listOf("Longbow[ColossusSlayer]", "Longbow", "Hail of Thorns"),
        listOf("Longbow", "Longbow", "Hail of Thorns"),
        listOf("Longbow[ColossusSlayer]", "Longbow", "Hail of Thorns"),
        listOf("Longbow", "Longbow", "Hail of Thorns"),
    )

    val hunterBestRangeL9 = listOf(
        listOf("Longbow", "Longbow", "Hail of Thorns"),
        listOf("Longbow[ColossusSlayer]", "Longbow", "Hail of Thorns"),
        listOf("Conjure Animals"),
        listOf("Longbow[ColossusSlayer]", "Longbow", "Hail of Thorns"),
        listOf("Longbow", "Longbow", "Hail of Thorns"),
    )

    // --------------------------------------------------------------------------
    // RANGE: GloomStalker

    val gsBestRangeL3 = listOf(
        listOf("Longbow[DreadfulStrike]", "Hunter's Mark"),
        listOf("Longbow[DreadfulStrike]"),      //        listOf("Longbow[DreadfulStrike]", "Hail of Thorns"),
        listOf("Longbow", "Hail of Thorns"),
        listOf("Longbow", "Hail of Thorns"),
        listOf("Longbow", "Hail of Thorns"),
    )

    val gsBestRangeL4 = listOf(
        listOf("Longbow[DreadfulStrike]", "Ensnaring Strike"), // Ensnaring instead of HM ?  because of higher WIS ?
        listOf("Longbow[DreadfulStrike]"),
        listOf("Longbow[DreadfulStrike]"),
        listOf("Longbow", "Hail of Thorns"),
        listOf("Longbow", "Hail of Thorns"),
    )

    val gsBestRangeL5 = listOf(
        listOf("Longbow[DreadfulStrike]", "Longbow", "Hunter's Mark"),
        listOf("Longbow[DreadfulStrike]", "Longbow", "Hail of Thorns"),
        listOf("Longbow[DreadfulStrike]", "Longbow", "Hail of Thorns"),
        listOf("Longbow", "Longbow", "Hail of Thorns"),
        listOf("Longbow", "Longbow", "Hail of Thorns"),
    )

    val gsBestRangeL8 = gsBestRangeL5.withIndex().map { (index, value) -> if (index != 3) value else gsBestRangeL5[1] }
    val gsBestRangeL9 = listOf(
        listOf("Longbow[DreadfulStrike]", "Longbow", "Hail of Thorns"),
        listOf("Longbow[DreadfulStrike]", "Longbow", "Hail of Thorns"),
        listOf("Longbow[DreadfulStrike]", "Longbow", "Hail of Thorns"),
        listOf("Longbow[DreadfulStrike]", "Longbow", "Hail of Thorns"),
        listOf("Conjure Animals"),
    )

    val gsBestRangeL12 = listOf(
        listOf("Longbow[DreadfulStrike]", "Longbow", "Hail of Thorns"),
        listOf("Longbow[DreadfulStrike]", "Longbow", "Hail of Thorns"),
        listOf("Longbow[DreadfulStrike]", "Longbow", "Hail of Thorns"),
        listOf("Conjure Animals"),
        listOf("Longbow[DreadfulStrike]", "Longbow", "Hail of Thorns"),
    )
    // --------------------------------------------------------------------------
    // RANGE: WinterWalker

    val wwBestRangeL3 = listOf(
        listOf("Longbow[PolarStrikes]", "Hunter's Mark"),
        listOf("Longbow[PolarStrikes]"),
        listOf("Longbow[PolarStrikes]", "Hail of Thorns"),
        listOf("Longbow[PolarStrikes]", "Hail of Thorns"),
        listOf("Longbow[PolarStrikes]", "Hail of Thorns"),
    )

    val wwBestRangeL4 =listOf(
        listOf("Longbow[PolarStrikes]", "Ensnaring Strike"), // Ensnaring instead of HM ?  because of higher WIS ?
        listOf("Longbow[PolarStrikes]"),
        listOf("Longbow[PolarStrikes]"),
        listOf("Longbow[PolarStrikes]", "Hail of Thorns"),
        listOf("Longbow[PolarStrikes]", "Hail of Thorns"),
    )

    // WW+CS has lower WIS than plain WW starting at L4; hence, WW+CS continues using Hunter's Mark a little longer ...?
    val wwCsBestRangeL4 = wwBestRangeL3.withIndex().map { (index, value) ->
        if (index != 2) value
        else listOf("Longbow[PolarStrikes]","Hail of Thorns") }

    val wwBestRangeL5 =listOf(
        listOf("Hold Person"), // Ensnaring instead of HM ?  because of higher WIS ?
        listOf("Longbow[PolarStrikes]", "Longbow", "Hail of Thorns"),
        listOf("Longbow[PolarStrikes]", "Longbow", "Hail of Thorns"),
        listOf("Longbow[PolarStrikes]", "Longbow", "Hail of Thorns"),
        listOf("Longbow[PolarStrikes]", "Longbow", "Hail of Thorns"),
    )

    val wwBestRangeL9 =listOf(
        listOf("Longbow[PolarStrikes]", "Longbow", "Hail of Thorns"),
        listOf("Longbow[PolarStrikes]", "Longbow", "Hail of Thorns"),
        listOf("Longbow[PolarStrikes]", "Longbow", "Hail of Thorns"),
        listOf("Longbow[PolarStrikes]", "Longbow", "Hail of Thorns"),
        listOf("Conjure Animals"),
    )

    val wwCsBestRangeL12 =listOf(
        listOf("Longbow[PolarStrikes]", "Longbow", "Hail of Thorns"),
        listOf("Conjure Animals"),
        listOf("Longbow[PolarStrikes]", "Longbow", "Hail of Thorns"),
        listOf("Longbow[PolarStrikes]", "Longbow", "Hail of Thorns"),
        listOf("Longbow[PolarStrikes]", "Longbow", "Hail of Thorns"),
    )

    // --------------------------------------------------------------------------

    fun bestFiveTurnResult(character: Character, range: Int): ScenarioResult {
        val scenarioList = ScenarioBuilder(character, Globals.getMonster("Goblin"))
            .build(range, 5, DEFAULT_NUM_TARGETS, DEFAULT_TARGET_RADIUS)

        val scenarioResultList = scenarioList.map { ScenarioCalculator(it).calculateDPRForAllTurns() }.toList()
        val topResult = ScenarioResult.topResults(scenarioResultList, 1)[0]
        logger.info  { "${character.getName()} : bestDPR = ${ topResult.totalDPR } , attacks = ${ topResult.getAttackNames() }" }
        return topResult
    }

    // --------------------------------------------------------------------------
    // LEVEL 3

    @Test
    @EnabledIfSystemProperty(named = "RunSlowTests", matches = "true")
    fun bestMeleeDprLevel3() {
        listOf(hunterPlan, gsPlan, wwPlan, wwCSPlan).forEach { it.editableFields.level = 3 }

        var topResult: ScenarioResult
        topResult = bestFiveTurnResult(hunterPlan, Constants.MELEE_RANGE)
        assertEquals(70.041504f, topResult.totalDPR)
        assertEquals(hunterBestMelee, topResult.getAttackNames())

        topResult = bestFiveTurnResult(gsPlan, Constants.MELEE_RANGE)
        assertEquals(72.9465f, topResult.totalDPR)
        assertEquals(gsBestMeleeL3, topResult.getAttackNames())

        topResult = bestFiveTurnResult(wwPlan, Constants.MELEE_RANGE)
        assertEquals(72.489f, topResult.totalDPR)
        assertEquals(wwBestMelee, topResult.getAttackNames())

        topResult = bestFiveTurnResult(wwCSPlan, Constants.MELEE_RANGE)
        assertEquals(72.489f, topResult.totalDPR)
        assertEquals(wwBestMelee, topResult.getAttackNames())
    }

    @Test
    @EnabledIfSystemProperty(named = "RunSlowTests", matches = "true")
    fun bestRangeDprLevel3() {
        listOf(hunterPlan, gsPlan, wwPlan, wwCSPlan).forEach { it.editableFields.level = 3 }

        var topResult: ScenarioResult
        topResult = bestFiveTurnResult(hunterPlan, Constants.MELEE_RANGE*2)
        assertEquals(59.675f, topResult.totalDPR)
        assertEquals(hunterBestRangeL3, topResult.getAttackNames())

        topResult = bestFiveTurnResult(gsPlan, Constants.MELEE_RANGE*2)
        assertEquals(63.425003f, topResult.totalDPR)
        assertEquals(gsBestRangeL3, topResult.getAttackNames())

        topResult = bestFiveTurnResult(wwPlan, Constants.MELEE_RANGE*2)
        assertEquals(62.300003f, topResult.totalDPR)
        assertEquals(wwBestRangeL3, topResult.getAttackNames())

        topResult = bestFiveTurnResult(wwCSPlan, Constants.MELEE_RANGE*2)
        assertEquals(62.300003f, topResult.totalDPR)
        assertEquals(wwBestRangeL3, topResult.getAttackNames())
    }

    // --------------------------------------------------------------------------
    // LEVEL 4

    @Test
    @EnabledIfSystemProperty(named = "RunSlowTests", matches = "true")
    fun bestMeleeDprLevel4() {
        listOf(hunterPlan, gsPlan, wwPlan, wwCSPlan).forEach { it.editableFields.level = 4 }

        var topResult: ScenarioResult
        topResult = bestFiveTurnResult(hunterPlan, Constants.MELEE_RANGE)
        assertEquals(78.776245f, topResult.totalDPR)                // +8 over L3, due to higher Dex
        assertEquals(hunterBestMelee, topResult.getAttackNames())   // same attack sequence as level 3

        topResult = bestFiveTurnResult(gsPlan, Constants.MELEE_RANGE)
        assertEquals(78.703995f, topResult.totalDPR)                // +6 over L3, due to higher Wis (Dreadful Strike)
        assertEquals(gsBestMeleeL4, topResult.getAttackNames())

        // at L4, no change in melee damage output or attack sequence for WW or WW+CS
        topResult = bestFiveTurnResult(wwPlan, Constants.MELEE_RANGE)
        assertEquals(72.489f, topResult.totalDPR)
        assertEquals(wwBestMelee, topResult.getAttackNames())

        topResult = bestFiveTurnResult(wwCSPlan, Constants.MELEE_RANGE)
        assertEquals(72.489f, topResult.totalDPR)
        assertEquals(wwBestMelee, topResult.getAttackNames())
    }

    @Test
    @EnabledIfSystemProperty(named = "RunSlowTests", matches = "true")
    fun bestRangeDprLevel4() {
        listOf(hunterPlan, gsPlan, wwPlan, wwCSPlan).forEach { it.editableFields.level = 4 }

        var topResult: ScenarioResult
        topResult = bestFiveTurnResult(hunterPlan, Constants.MELEE_RANGE*2)
        assertEquals(66.700005f, topResult.totalDPR)                        // +7 over L3, due to higher Dex
        assertEquals(hunterBestRangeL3, topResult.getAttackNames())

        topResult = bestFiveTurnResult(gsPlan, Constants.MELEE_RANGE*2)     // +7 over L3, due to higher Wis (Dreadful Strike)
        assertEquals(70.01082f, topResult.totalDPR)
        assertEquals(gsBestRangeL4, topResult.getAttackNames())

        topResult = bestFiveTurnResult(wwPlan, Constants.MELEE_RANGE*2)
        assertEquals(62.966305f, topResult.totalDPR)                        // about +0.6 improvement over L3 (negligible)
        assertEquals(wwBestRangeL4, topResult.getAttackNames())

        topResult = bestFiveTurnResult(wwCSPlan, Constants.MELEE_RANGE*2)
        assertEquals(62.300003f, topResult.totalDPR)                        // WW+CS falls behind plain WW, due to lower WIS
        assertEquals(wwCsBestRangeL4, topResult.getAttackNames())
    }

    // --------------------------------------------------------------------------
    // LEVEL 5
    //
    //      Ranger gets an Extra Attack at L5, so DPR makes a pretty big jump
    //

    @Test
    @EnabledIfSystemProperty(named = "RunSlowTests", matches = "true")
    fun bestMeleeDprLevel5() {
        listOf(hunterPlan, gsPlan, wwPlan, wwCSPlan).forEach { it.editableFields.level = 5 }

        var topResult: ScenarioResult
        topResult = bestFiveTurnResult(hunterPlan, Constants.MELEE_RANGE)
        assertEquals(134.00911f, topResult.totalDPR)                    // L5: roughly 2x the DPR of L3
        assertEquals(hunterBestMeleeL5, topResult.getAttackNames())     // L5: extra attacks

        topResult = bestFiveTurnResult(gsPlan, Constants.MELEE_RANGE)
        assertEquals(127.86662f, topResult.totalDPR)
        assertEquals(gsBestMeleeL5, topResult.getAttackNames())

        topResult = bestFiveTurnResult(wwPlan, Constants.MELEE_RANGE)
        assertEquals(121.201614f, topResult.totalDPR)
        assertEquals(wwBestMeleeL5, topResult.getAttackNames())

        topResult = bestFiveTurnResult(wwCSPlan, Constants.MELEE_RANGE)
        assertEquals(121.201614f, topResult.totalDPR)
        assertEquals(wwBestMeleeL5, topResult.getAttackNames())
    }

    @Test
    @EnabledIfSystemProperty(named = "RunSlowTests", matches = "true")
    fun bestRangeDprLevel5() {
        listOf(hunterPlan, gsPlan, wwPlan, wwCSPlan).forEach { it.editableFields.level = 5 }

        var topResult: ScenarioResult
        topResult = bestFiveTurnResult(hunterPlan, Constants.MELEE_RANGE*2)
        assertEquals(125.69999f, topResult.totalDPR)
        assertEquals(hunterBestRangeL5, topResult.getAttackNames())     // all range attack sequences change at L5

        topResult = bestFiveTurnResult(gsPlan, Constants.MELEE_RANGE*2)
        assertEquals(121.79999f, topResult.totalDPR)
        assertEquals(gsBestRangeL5, topResult.getAttackNames())

        topResult = bestFiveTurnResult(wwPlan, Constants.MELEE_RANGE*2)
        assertEquals(131.275f, topResult.totalDPR)
        assertEquals(wwBestRangeL5, topResult.getAttackNames())

        topResult = bestFiveTurnResult(wwCSPlan, Constants.MELEE_RANGE*2)
        assertEquals(130.1125f, topResult.totalDPR)
        assertEquals(wwBestRangeL5, topResult.getAttackNames())
    }

    // --------------------------------------------------------------------------
    // LEVEL 8

    @Test
    @EnabledIfSystemProperty(named = "RunSlowTests", matches = "true")
    fun bestMeleeDprLevel8() {
        listOf(hunterPlan, gsPlan, wwPlan, wwCSPlan).forEach { it.editableFields.level = 8 }

        var topResult: ScenarioResult
        topResult = bestFiveTurnResult(hunterPlan, Constants.MELEE_RANGE)
        assertEquals(134.00911f, topResult.totalDPR)                    // L8 hunter: same as L5
        assertEquals(hunterBestMeleeL5, topResult.getAttackNames())

        topResult = bestFiveTurnResult(gsPlan, Constants.MELEE_RANGE)
        assertEquals(134.01787f, topResult.totalDPR)                    // L8 GS: +6 over L5, thanks to WIS, DreadfulStrike
        assertEquals(gsBestMeleeL8, topResult.getAttackNames())

        topResult = bestFiveTurnResult(wwPlan, Constants.MELEE_RANGE)
        assertEquals(121.201614f, topResult.totalDPR)                   // L8 WW: same as L5
        assertEquals(wwBestMeleeL5, topResult.getAttackNames())

        topResult = bestFiveTurnResult(wwCSPlan, Constants.MELEE_RANGE)
        assertEquals(136.81436f, topResult.totalDPR)                    // L8 WW+CS = L5 + 15 !  // TODO: how ???
        assertEquals(wwBestMeleeL5, topResult.getAttackNames())
    }

    @Test
    @EnabledIfSystemProperty(named = "RunSlowTests", matches = "true")
    fun bestRangeDprLevel8() {
        listOf(hunterPlan, gsPlan, wwPlan, wwCSPlan).forEach { it.editableFields.level = 8 }

        var topResult: ScenarioResult
        topResult = bestFiveTurnResult(hunterPlan, Constants.MELEE_RANGE*2)
        assertEquals(126.299995f, topResult.totalDPR)
        assertEquals(hunterBestRangeL5, topResult.getAttackNames())     // all range attack sequences change at L5

        topResult = bestFiveTurnResult(gsPlan, Constants.MELEE_RANGE*2)
        assertEquals(127.99999f, topResult.totalDPR)
        assertEquals(gsBestRangeL8, topResult.getAttackNames())

        topResult = bestFiveTurnResult(wwPlan, Constants.MELEE_RANGE*2)
        assertEquals(132.4375f, topResult.totalDPR)
        assertEquals(wwBestRangeL5, topResult.getAttackNames())

        topResult = bestFiveTurnResult(wwCSPlan, Constants.MELEE_RANGE*2)
        assertEquals(142.384f, topResult.totalDPR)
        assertEquals(wwBestRangeL5, topResult.getAttackNames())
    }

    // --------------------------------------------------------------------------
    // LEVEL 9

    @Test
    @EnabledIfSystemProperty(named = "RunSlowTests", matches = "true")
    fun bestMeleeDprLevel9() {
        listOf(hunterPlan, gsPlan, wwPlan, wwCSPlan).forEach { it.editableFields.level = 9 }

        var topResult: ScenarioResult
        topResult = bestFiveTurnResult(hunterPlan, Constants.MELEE_RANGE)
        assertEquals(140.83624f, topResult.totalDPR)
        assertEquals(hunterBestMeleeL5, topResult.getAttackNames())

        topResult = bestFiveTurnResult(gsPlan, Constants.MELEE_RANGE)
        assertEquals(142.09912f, topResult.totalDPR)
        assertEquals(gsBestMeleeL8, topResult.getAttackNames())

        topResult = bestFiveTurnResult(wwPlan, Constants.MELEE_RANGE)
        assertEquals(128.49136f, topResult.totalDPR)
        assertEquals(wwBestMeleeL5, topResult.getAttackNames())

        topResult = bestFiveTurnResult(wwCSPlan, Constants.MELEE_RANGE)
        assertEquals(143.8125f, topResult.totalDPR)
        assertEquals(wwBestMeleeL5, topResult.getAttackNames())
    }

    @Test
    @EnabledIfSystemProperty(named = "RunSlowTests", matches = "true")
    fun bestRangeDprLevel9() {
        listOf(hunterPlan, gsPlan, wwPlan, wwCSPlan).forEach { it.editableFields.level = 9 }

        var topResult: ScenarioResult
        topResult = bestFiveTurnResult(hunterPlan, Constants.MELEE_RANGE*2)
        assertEquals(157.70001f, topResult.totalDPR)
        assertEquals(hunterBestRangeL9, topResult.getAttackNames())

        topResult = bestFiveTurnResult(gsPlan, Constants.MELEE_RANGE*2)
        assertEquals(167.925f, topResult.totalDPR)
        assertEquals(gsBestRangeL9, topResult.getAttackNames())

        topResult = bestFiveTurnResult(wwPlan, Constants.MELEE_RANGE*2)
        assertEquals(152.625f, topResult.totalDPR)
        assertEquals(wwBestRangeL9, topResult.getAttackNames())

        topResult = bestFiveTurnResult(wwCSPlan, Constants.MELEE_RANGE*2)
        assertEquals(158.6f, topResult.totalDPR)
        assertEquals(wwBestRangeL9, topResult.getAttackNames())
    }

    // --------------------------------------------------------------------------
    // LEVEL 12

    @Test
    @EnabledIfSystemProperty(named = "RunSlowTests", matches = "true")
    fun bestMeleeDprLevel12() {
        listOf(hunterPlan, gsPlan, wwPlan, wwCSPlan).forEach { it.editableFields.level = 12 }

        var topResult: ScenarioResult
        topResult = bestFiveTurnResult(hunterPlan, Constants.MELEE_RANGE)
        assertEquals(140.83624f, topResult.totalDPR)
        assertEquals(hunterBestMeleeL5, topResult.getAttackNames())

        topResult = bestFiveTurnResult(gsPlan, Constants.MELEE_RANGE)
        assertEquals(148.61087f, topResult.totalDPR)
        assertEquals(gsBestMeleeL12, topResult.getAttackNames())

        topResult = bestFiveTurnResult(wwPlan, Constants.MELEE_RANGE)
        assertEquals(128.49136f, topResult.totalDPR)
        assertEquals(wwBestMeleeL5, topResult.getAttackNames())

        topResult = bestFiveTurnResult(wwCSPlan, Constants.MELEE_RANGE)
        assertEquals(143.8125f, topResult.totalDPR)
        assertEquals(wwBestMeleeL5, topResult.getAttackNames())
    }

    @Test
    @EnabledIfSystemProperty(named = "RunSlowTests", matches = "true")
    fun bestRangeDprLevel12() {
        listOf(hunterPlan, gsPlan, wwPlan, wwCSPlan).forEach { it.editableFields.level = 12 }

        var topResult: ScenarioResult
        topResult = bestFiveTurnResult(hunterPlan, Constants.MELEE_RANGE*2)
        assertEquals(162.425f, topResult.totalDPR)
        assertEquals(hunterBestRangeL9, topResult.getAttackNames())

        topResult = bestFiveTurnResult(gsPlan, Constants.MELEE_RANGE*2)
        assertEquals(172.65001f, topResult.totalDPR)
        assertEquals(gsBestRangeL12, topResult.getAttackNames())

        topResult = bestFiveTurnResult(wwPlan, Constants.MELEE_RANGE*2)
        assertEquals(157.35f, topResult.totalDPR)
        assertEquals(wwBestRangeL9, topResult.getAttackNames())

        topResult = bestFiveTurnResult(wwCSPlan, Constants.MELEE_RANGE*2)
        assertEquals(163.32501f, topResult.totalDPR)
        assertEquals(wwCsBestRangeL12, topResult.getAttackNames())
    }
}