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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

//@EnabledIfSystemProperty(named = "RunSlowTests", matches = "true")
class DprTest {
    @Transient private val logger = LoggerFactory.get(DprTest::class.simpleName ?: "")


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

        for (level in listOf(3,4,5, 8,9,12, 13, 16,17)) {
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