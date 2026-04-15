package com.vikinghelmet.dnd.dpr

import com.vikinghelmet.dnd.dpr.TestUtil.gsPlan
import com.vikinghelmet.dnd.dpr.TestUtil.hunterPlan
import com.vikinghelmet.dnd.dpr.TestUtil.wwCSPlan
import com.vikinghelmet.dnd.dpr.TestUtil.wwPlan
import com.vikinghelmet.dnd.dpr.character.feats.Feat
import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import com.vikinghelmet.dnd.dpr.scenario.ScenarioBuilder
import com.vikinghelmet.dnd.dpr.scenario.ScenarioCalculator
import com.vikinghelmet.dnd.dpr.scenario.ScenarioResult
import com.vikinghelmet.dnd.dpr.util.Constants
import com.vikinghelmet.dnd.dpr.util.Constants.DEFAULT_NUM_TARGETS
import com.vikinghelmet.dnd.dpr.util.Constants.DEFAULT_TARGET_RADIUS
import com.vikinghelmet.dnd.dpr.util.Globals
import dev.shivathapaa.logger.api.LoggerFactory
import junit.framework.TestCase.assertEquals
import kotlinx.serialization.Transient
import kotlin.test.Test
import kotlin.test.assertTrue

class EditableCharacterTest {
    @Transient private val logger = LoggerFactory.get(EditableCharacterTest::class.simpleName ?: "")

    val allRangerSubclasses = listOf(hunterPlan, gsPlan, wwPlan, wwCSPlan)

    val hunterBestMelee = listOf(
        listOf("Shortsword", "Hunter's Mark"),
        listOf("Shortsword[ColossusSlayer]", "Shortsword"),
        listOf("Shortsword", "Shortsword"),
        listOf("Shortsword[ColossusSlayer]", "Shortsword"),
        listOf("Shortsword", "Shortsword"),
    )

    val gsBestMelee = listOf(
        listOf("Shortsword[DreadfulStrike]", "Hunter's Mark"),
        listOf("Shortsword[DreadfulStrike]", "Shortsword"),
        listOf("Shortsword", "Shortsword"),
        listOf("Shortsword", "Shortsword"),
        listOf("Shortsword", "Shortsword"),
    )

    val wwBestMelee = listOf(
        listOf("Shortsword[PolarStrikes]", "Hunter's Mark"),
        listOf("Shortsword[PolarStrikes]", "Shortsword"),
        listOf("Shortsword[PolarStrikes]", "Shortsword"),
        listOf("Shortsword[PolarStrikes]", "Shortsword"),
        listOf("Shortsword[PolarStrikes]", "Shortsword"),
    )

    @Test
    fun getNameTest() {
        assertEquals("Leif Lightfoot - Hunter", hunterPlan.getName())
        assertEquals("Leif Lightfoot - GS", gsPlan.getName())
        assertEquals("Leif Lightfoot - Winter Walker", wwPlan.getName())
        assertEquals("Leif Lightfoot - Winter Walker + Cold Caster", wwCSPlan.getName())
    }

    fun bestFiveTurnResult(character: com.vikinghelmet.dnd.dpr.character.Character, range: Int): ScenarioResult {
        val scenarioList = ScenarioBuilder(character, Globals.getMonster("Goblin"))
            .build(range, 5, DEFAULT_NUM_TARGETS, DEFAULT_TARGET_RADIUS)

        val scenarioResultList = scenarioList.map { ScenarioCalculator(it).calculateDPRForAllTurns() }.toList()
        val topResult = ScenarioResult.topResults(scenarioResultList, 1)[0]
        logger.info  { "${character.getName()} : bestDPR = ${ topResult.totalDPR } , attacks = ${ topResult.getAttackNames() }" }
        return topResult
    }

    @Test
    fun level3() {
        // baseline: all stats the same at level 3
        listOf(hunterPlan, gsPlan, wwPlan, wwCSPlan).forEach {
            it.editableFields.level = 3
            assertEquals(19, it.getModifiedAbilityScore(AbilityType.Dexterity))
            assertEquals(14, it.getModifiedAbilityScore(AbilityType.Wisdom))
            assertEquals(12, it.getModifiedAbilityScore(AbilityType.Strength))
            assertEquals(listOf(Feat.Archery), it.getFeatList())
        }

        var topResult: ScenarioResult

        topResult = bestFiveTurnResult(hunterPlan, Constants.MELEE_RANGE)
        assertEquals(70.041504f, topResult.totalDPR)
        assertEquals(hunterBestMelee, topResult.getAttackNames())

        topResult = bestFiveTurnResult(gsPlan, Constants.MELEE_RANGE)
        assertEquals(72.9465f, topResult.totalDPR)
        assertEquals(gsBestMelee, topResult.getAttackNames())

        topResult = bestFiveTurnResult(wwPlan, Constants.MELEE_RANGE)
        assertEquals(72.489f, topResult.totalDPR)
        assertEquals(wwBestMelee, topResult.getAttackNames())

        topResult = bestFiveTurnResult(wwCSPlan, Constants.MELEE_RANGE)
        assertEquals(72.489f, topResult.totalDPR)
        assertEquals(wwBestMelee, topResult.getAttackNames())

        logger.info { "" }
        logger.info {"" }
        logger.info {"" }

        topResult = bestFiveTurnResult(hunterPlan, Constants.MELEE_RANGE*2)
        assertEquals(59.675f, topResult.totalDPR)
        //assertEquals(hunterBestRange, topResult.getAttackNames())

        topResult = bestFiveTurnResult(gsPlan, Constants.MELEE_RANGE*2)
        assertEquals(63.425003f, topResult.totalDPR)
        //assertEquals(gsBestRange, topResult.getAttackNames())

        topResult = bestFiveTurnResult(wwPlan, Constants.MELEE_RANGE*2)
        assertEquals(62.300003f, topResult.totalDPR)
        //assertEquals(wwBestRange, topResult.getAttackNames())

        topResult = bestFiveTurnResult(wwCSPlan, Constants.MELEE_RANGE*2)
        assertEquals(62.300003f, topResult.totalDPR)
        //assertEquals(wwBestRange, topResult.getAttackNames())

    }

    @Test
    fun level4() {
        // first ASI bump occurs at level 4
        allRangerSubclasses.forEach { it.editableFields.level = 4 }

        // the hunter prioritizes Dex
        listOf(hunterPlan).forEach {
            assertTrue(
                it.getFeatList().contains(Feat.Sharpshooter)
            ) // some unique benefits, and a single point bump in Dex
            assertEquals(20, it.getModifiedAbilityScore(AbilityType.Dexterity)) // +1
        }

        // GS and WW prioritize Wis over Dex
        listOf(gsPlan, wwPlan).forEach {
            assertEquals(16, it.getModifiedAbilityScore(AbilityType.Wisdom))    // +2
        }

        // WW CS = the CS feat confers some unique benefits, but only a single point bump in Wis
        listOf(wwCSPlan).forEach {
            assertTrue(it.getFeatList().contains(Feat.ColdCaster))
            assertEquals(15, it.getModifiedAbilityScore(AbilityType.Wisdom))    // +1
        }
    }

    @Test
    fun level8() {
        allRangerSubclasses.forEach { it.editableFields.level = 8 }

        listOf(hunterPlan).forEach {
            assertEquals(16, it.getModifiedAbilityScore(AbilityType.Wisdom))    // +2
        }

        // GS and WW prioritize Wis over Dex
        listOf(gsPlan, wwPlan).forEach {
            assertEquals(18, it.getModifiedAbilityScore(AbilityType.Wisdom))    // +2
        }

        // For WW CS, since both Dex and Wis were odd numbers, add 1 to both (ability benefits come with even numbers)
        listOf(wwCSPlan).forEach {
            assertEquals(16, it.getModifiedAbilityScore(AbilityType.Wisdom))    // +1
            assertEquals(20, it.getModifiedAbilityScore(AbilityType.Dexterity)) // +1
        }
    }

    @Test
    fun level12() {
        allRangerSubclasses.forEach { it.editableFields.level = 12 }

        listOf(hunterPlan, wwCSPlan).forEach {
            assertEquals(18, it.getModifiedAbilityScore(AbilityType.Wisdom))    // +2
        }

        listOf(gsPlan, wwPlan).forEach {
            assertEquals(20, it.getModifiedAbilityScore(AbilityType.Wisdom))    // +2
        }
    }

    @Test
    fun level16() {
        allRangerSubclasses.forEach { it.editableFields.level = 16 }

        listOf(hunterPlan).forEach {
            assertTrue(it.getFeatList().contains(Feat.Piercer))
            assertEquals(13, it.getModifiedAbilityScore(AbilityType.Strength))    // +1
        }

        listOf(gsPlan, wwPlan).forEach {
            assertTrue(it.getFeatList().contains(Feat.Piercer))
            assertEquals(20, it.getModifiedAbilityScore(AbilityType.Dexterity))   // +1
        }

        listOf(wwCSPlan).forEach {
            assertEquals(20, it.getModifiedAbilityScore(AbilityType.Wisdom))    // +2
        }
    }
}