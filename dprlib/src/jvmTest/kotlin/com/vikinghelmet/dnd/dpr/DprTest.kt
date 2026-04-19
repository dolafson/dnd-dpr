package com.vikinghelmet.dnd.dpr

import com.vikinghelmet.dnd.dpr.TestUtil.gsPlan
import com.vikinghelmet.dnd.dpr.TestUtil.hunterPlan
import com.vikinghelmet.dnd.dpr.TestUtil.wwCSPlan
import com.vikinghelmet.dnd.dpr.TestUtil.wwPlan
import com.vikinghelmet.dnd.dpr.editable.EditableCharacter
import com.vikinghelmet.dnd.dpr.scenario.ScenarioBuilder
import com.vikinghelmet.dnd.dpr.scenario.ScenarioCalculator
import com.vikinghelmet.dnd.dpr.scenario.ScenarioResult
import com.vikinghelmet.dnd.dpr.util.Constants.DEFAULT_NUM_TARGETS
import com.vikinghelmet.dnd.dpr.util.Constants.DEFAULT_TARGET_RADIUS
import com.vikinghelmet.dnd.dpr.util.Constants.MELEE_RANGE
import com.vikinghelmet.dnd.dpr.util.Globals
import dev.shivathapaa.logger.api.LoggerFactory
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

//@EnabledIfSystemProperty(named = "RunSlowTests", matches = "true")
class DprTest {
    @Transient private val logger = LoggerFactory.get(DprTest::class.simpleName ?: "")

    @Serializable
    class SimpleResult(
        val totalDPR: Int,
        var attacks: List<List<String>>? = emptyList(),
        val clone: Int? = null,
        val level: Int? = null,
    )
    {
        constructor(sr: ScenarioResult) : this(sr.totalDPR.toInt(), sr.getAttackNames())

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as SimpleResult
            return totalDPR == other.totalDPR && attacks == other.attacks
        }

        override fun hashCode(): Int {
            var result = totalDPR
            result = 31 * result + attacks.hashCode()
            return result
        }

        override fun toString(): String {
            return "(totalDPR=$totalDPR, attacks=$attacks)"
        }
    }

    fun bestDPR (level: Int, character: EditableCharacter, range: Int): SimpleResult {
        character.editableFields.level = level
        val scenarioList = ScenarioBuilder(character, Globals.getMonster("Goblin"))
            .build(range, 5, DEFAULT_NUM_TARGETS, DEFAULT_TARGET_RADIUS)

        val scenarioResultList = scenarioList.map { ScenarioCalculator(it).calculateDPRForAllTurns() }.toList()
        val topResult = ScenarioResult.topResults(scenarioResultList, 1)[0]
        logger.info  { "${character.getName()} : bestDPR = ${ topResult.totalDPR.toInt() } , attacks = ${ topResult.getAttackNames() }" }
        return SimpleResult(topResult)
    }

    fun getExpectedResults(name: String): List<SimpleResult> {
        val json = TestUtil.getResource("expectedResults/$name.json")
        val expected: List<SimpleResult> = Json.Default.decodeFromString(json!!)

        expected.forEach { it ->
            if (it.clone != null) it.attacks = expected.first { it2 -> it2.level == it.clone }.attacks
        }
        return expected
    }

    // --------------------------------------------------------------------------

    @Test
    fun hunterMelee() {
        val expectedList = getExpectedResults("hunterMelee")

        for (level in listOf(3,4,5, 8,9,12,13, 16,17)) {
            val expected = expectedList.find { it.level == level }
            assertEquals (expected, bestDPR(level, hunterPlan, MELEE_RANGE), "hunter"+level)
        }
    }

    @Test
    fun hunterRange() {
        val expectedList = getExpectedResults("hunterRange")

        for (level in listOf(3,4,5, 8,9,12,13, 16,17)) {
            val expected = expectedList.find { it.level == level }
            assertEquals (expected, bestDPR(level, hunterPlan, MELEE_RANGE*2), "hunter"+level)
        }
    }

    // --------------------------------------------------------------------------

    @Test
    fun gsMelee() {
        val expectedList = getExpectedResults("gsMelee")

        for (level in listOf(3,4,5, 8,9,12,13, 16,17)) {
            val expected = expectedList.find { it.level == level }
            assertEquals (expected, bestDPR(level, gsPlan, MELEE_RANGE), "gs"+level)
        }
    }

    @Test
    fun gsRange() {
        val expectedList = getExpectedResults("gsRange")

        for (level in listOf(3,4,5, 8,9,12,13, 16,17)) {
            val expected = expectedList.find { it.level == level }
            assertEquals (expected, bestDPR(level, gsPlan, MELEE_RANGE*2), "gs"+level)
        }
    }

    // --------------------------------------------------------------------------

    @Test
    fun wwMelee() {
        val expectedList = getExpectedResults("wwMelee")

        for (level in listOf(3,4,5, 8,9,12,13, 16,17)) {
            val expected = expectedList.find { it.level == level }
            assertEquals (expected, bestDPR(level, wwPlan, MELEE_RANGE), "ww"+level)
        }
    }

    @Test
    fun wwRange() {
        val expectedList = getExpectedResults("wwRange")

        for (level in listOf(3,4,5, 8,9,12,13, 16,17)) {
            val expected = expectedList.find { it.level == level }
            assertEquals (expected, bestDPR(level, wwPlan, MELEE_RANGE*2), "ww"+level)
        }
    }

    // --------------------------------------------------------------------------

    @Test
    fun wwCsMelee() {
        val expectedList = getExpectedResults("wwCsMelee")

        for (level in listOf(3,4,5, 8,9,12,13, 16,17)) {
            val expected = expectedList.find { it.level == level }
            assertEquals (expected, bestDPR(level, wwCSPlan, MELEE_RANGE), "wwCs"+level)
        }
    }

    @Test
    fun wwCsRange() {
        val expectedList = getExpectedResults("wwCsRange")

        for (level in listOf(3,4,5, 8,9,12,13, 16,17)) {
            val expected = expectedList.find { it.level == level }
            assertEquals (expected, bestDPR(level, wwCSPlan, MELEE_RANGE*2), "wwCs"+level)
        }
    }
}