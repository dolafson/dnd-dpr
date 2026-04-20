package com.vikinghelmet.dnd.dpr

import com.vikinghelmet.dnd.dpr.TestUtil.bestDPR
import com.vikinghelmet.dnd.dpr.TestUtil.getExpectedResults
import com.vikinghelmet.dnd.dpr.TestUtil.gsPlan
import com.vikinghelmet.dnd.dpr.TestUtil.hunterPlan
import com.vikinghelmet.dnd.dpr.TestUtil.wwCSPlan
import com.vikinghelmet.dnd.dpr.TestUtil.wwPlan
import com.vikinghelmet.dnd.dpr.util.Constants.MELEE_RANGE
import dev.shivathapaa.logger.api.LoggerFactory
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@EnabledIfSystemProperty(named = "RunSlowTests", matches = "true")
class DprTest {
    @Transient private val logger = LoggerFactory.get(DprTest::class.simpleName ?: "")

    fun dprTestInner(targetType: String, rangeType: String, subclass: String, runAssert: Boolean) {
        val scenarioName = "$targetType/$rangeType/$subclass.json"
        val range        = if (rangeType == "melee") MELEE_RANGE else MELEE_RANGE*2
        val numTargets   = if (targetType == "singleTarget") 1 else 4
        val expectedList = getExpectedResults(scenarioName)
        val character    = when (subclass) {
            "hunter" -> hunterPlan
            "gs"    -> gsPlan
            "ww"    -> wwPlan
            "wwCs"  -> wwCSPlan
            else -> throw IllegalArgumentException("Unsupported subclass: $subclass")
        }
        logger.info { "Testing $scenarioName" }

        for (level in listOf(3,4,5, 8,9,12,13, 16,17)) {
            val bestResult = bestDPR(level, character, numTargets, range)
            if (!runAssert) {
                bestResult.level = level
                //println(Json { prettyPrint = true }.encodeToString(bestResult))
                println(Json.encodeToString(bestResult))
            } else {
                logger.info { "${rangeType}:${subclass}:${level}" }
                assertEquals (expectedList.find { it.level == level }, bestResult, "${rangeType}:${subclass}:${level}")
            }
        }
    }

    // REMINDER: if running these manually in intellij, remember to comment out EnabledIfSystemProperty above

 //   @Test
 //   fun oneOff() { dprTestInner("singleTarget", "melee", "wwCs", true)  }

    @ParameterizedTest
    @ValueSource(strings = [
        "multipleTarget/melee/hunter.json",
        "multipleTarget/melee/wwCs.json",
        "multipleTarget/melee/ww.json",
        "multipleTarget/melee/gs.json",

        "multipleTarget/range/hunter.json",
        "multipleTarget/range/wwCs.json",
        "multipleTarget/range/ww.json",
        "multipleTarget/range/gs.json",

        "singleTarget/melee/hunter.json",
        "singleTarget/melee/wwCs.json",
        "singleTarget/melee/ww.json",
        "singleTarget/melee/gs.json",

        "singleTarget/range/hunter.json",
        "singleTarget/range/wwCs.json",
        "singleTarget/range/ww.json",
        "singleTarget/range/gs.json"
    ])
    fun testParam(path: String) {
        val arr = path.split("/")
        dprTestInner(arr[0], arr[1], arr[2].replace(".json",""), true)
    }
}