package com.vikinghelmet.dnd.dpr

import com.vikinghelmet.dnd.dpr.TestUtil.bestDPR
import com.vikinghelmet.dnd.dpr.TestUtil.getExpectedResults
import com.vikinghelmet.dnd.dpr.TestUtil.gsDexPlan
import com.vikinghelmet.dnd.dpr.TestUtil.gsPlan
import com.vikinghelmet.dnd.dpr.TestUtil.hunterPlan
import com.vikinghelmet.dnd.dpr.TestUtil.wwCCPlan
import com.vikinghelmet.dnd.dpr.TestUtil.wwPlan
import com.vikinghelmet.dnd.dpr.util.Constants.MELEE_RANGE
import dev.shivathapaa.logger.api.LoggerFactory
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class DprTest {
    @Transient private val logger = LoggerFactory.get(DprTest::class.simpleName ?: "")

    @Test
    fun planTest() {
        listOf(hunterPlan, gsPlan, gsDexPlan, wwPlan, wwCCPlan).forEach { plan ->
            assertEquals(20, plan.editableFields.plan.size)
        }
    }

    fun dprTestInner(targetType: String, rangeType: String, subclass: String, levels: List<Int>, runAssert: Boolean) {
        val scenarioName = "$targetType/$rangeType/$subclass.json"
        val range        = if (rangeType == "melee") MELEE_RANGE else MELEE_RANGE*2
        val numTargets   = if (targetType == "singleTarget") 1 else 4
        val expectedList = getExpectedResults(scenarioName)
        val character    = when (subclass) {
            "hunter" -> hunterPlan
            "gs"    -> gsPlan
            "gsDex" -> gsDexPlan
            "ww"    -> wwPlan
            "wwCC"  -> wwCCPlan
            else -> throw IllegalArgumentException("Unsupported subclass: $subclass")
        }
        logger.info { "Testing $scenarioName" }

        for (level in levels) {
            val bestResult = bestDPR(level, character, numTargets, range)
            if (!runAssert) {
                bestResult.level = level
                //println(Json { prettyPrint = true }.encodeToString(bestResult))
                println(Json.encodeToString(bestResult)+",")
            } else {
                logger.info { "${rangeType}:${subclass}:${level}" }
                assertEquals (expectedList.find { it.level == level }, bestResult, "${rangeType}:${subclass}:${level}")
            }
        }
    }

    @Test
    fun oneOff() {
        //dprTestInner("singleTarget", "range", "gs", listOf(3,4,5, 8,9,12,13, 16,17), true)
        dprTestInner("multipleTarget", "range", "wwCC", listOf(5), true)
    }

    @EnabledIfSystemProperty(named = "RunSlowTests", matches = "true")
    @ParameterizedTest
    @ValueSource(strings = [
        "multipleTarget/melee/hunter.json",
        "multipleTarget/melee/wwCC.json",
        "multipleTarget/melee/ww.json",
        "multipleTarget/melee/gs.json",
        "multipleTarget/melee/gsDex.json",

        "multipleTarget/range/hunter.json",
        "multipleTarget/range/wwCC.json",
        "multipleTarget/range/ww.json",
        "multipleTarget/range/gs.json",
        "multipleTarget/range/gsDex.json",

        "singleTarget/melee/hunter.json",
        "singleTarget/melee/wwCC.json",
        "singleTarget/melee/ww.json",
        "singleTarget/melee/gs.json",
        "singleTarget/melee/gsDex.json",

        "singleTarget/range/hunter.json",
        "singleTarget/range/wwCC.json",
        "singleTarget/range/ww.json",
        "singleTarget/range/gs.json",
        "singleTarget/range/gsDex.json"
    ])
    fun testParam(path: String) {
        val arr = path.split("/")
        dprTestInner(arr[0], arr[1], arr[2].replace(".json",""), listOf(3,4,5, 8,9,12,13, 16,17), true)
    }
}