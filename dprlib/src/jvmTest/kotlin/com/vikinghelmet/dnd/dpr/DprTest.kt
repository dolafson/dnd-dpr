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

    val hunterBestMeleeL13 = listOf(
        listOf("Shortsword", "Shortsword", "Shortsword"),
        listOf("Shortsword[ColossusSlayer]", "Shortsword", "Shortsword"),
        listOf("Shortsword", "Shortsword", "Shortsword"),
        listOf("Shortsword[ColossusSlayer]", "Shortsword", "Shortsword"),
        listOf("Conjure Woodland Beings"),
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

    val gsBestMeleeL13 = listOf(listOf("Conjure Woodland Beings")) + List(4) { listOf("Shortsword[DreadfulStrike]", "Shortsword", "Shortsword")}

    // --------------------------------------------------------------------------
    // MELEE: WinterWalker

    val wwBestMelee = listOf(listOf("Shortsword[PolarStrikes]", "Hunter's Mark")) +
                   List(4) { listOf("Shortsword[PolarStrikes]", "Shortsword",) }

    val wwBestMeleeL5 = listOf(listOf("Shortsword[PolarStrikes]", "Shortsword", "Hunter's Mark")) +
                     List(4) { listOf("Shortsword[PolarStrikes]", "Shortsword", "Shortsword",) }
    val wwBestMeleeL13 = listOf(listOf("Conjure Woodland Beings")) + List(4) { listOf("Shortsword[PolarStrikes]", "Shortsword", "Shortsword")}

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

    val hunterBestRangeL13 = listOf(
        listOf("Longbow", "Longbow", "Hail of Thorns"),
        listOf("Longbow[ColossusSlayer]", "Longbow", "Hail of Thorns"),
        listOf("Longbow", "Longbow", "Hail of Thorns"),
        listOf("Longbow[ColossusSlayer]", "Longbow", "Hail of Thorns"),
        listOf("Conjure Animals"),
    )

    // --------------------------------------------------------------------------
    // RANGE: GloomStalker

    val gsBestRangeL3 = listOf(
        listOf("Longbow[DreadfulStrike]", "Hunter's Mark"),
        listOf("Longbow[DreadfulStrike]"),
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
    val gsBestRangeL9 =  List(4) { listOf("Longbow[DreadfulStrike]", "Longbow", "Hail of Thorns")} + listOf(listOf("Conjure Animals"))

    val gsBestRangeL12 = listOf(
        listOf("Longbow[DreadfulStrike]", "Longbow", "Hail of Thorns"),
        listOf("Longbow[DreadfulStrike]", "Longbow", "Hail of Thorns"),
        listOf("Longbow[DreadfulStrike]", "Longbow", "Hail of Thorns"),
        listOf("Conjure Animals"),
        listOf("Longbow[DreadfulStrike]", "Longbow", "Hail of Thorns"),
    )

    val gsBestRangeL13 = gsBestRangeL9

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

    val wwBestRangeL5 = listOf(listOf("Hold Person")) +
                        List(4) { listOf("Longbow[PolarStrikes]", "Longbow", "Hail of Thorns")}
    val wwBestRangeL9 = List(4) { listOf("Longbow[PolarStrikes]", "Longbow", "Hail of Thorns")} +
                        listOf(listOf("Conjure Animals"))
    val wwCsBestRangeL12 =listOf(
        listOf("Longbow[PolarStrikes]", "Longbow", "Hail of Thorns"),
        listOf("Conjure Animals"),
        listOf("Longbow[PolarStrikes]", "Longbow", "Hail of Thorns"),
        listOf("Longbow[PolarStrikes]", "Longbow", "Hail of Thorns"),
        listOf("Longbow[PolarStrikes]", "Longbow", "Hail of Thorns"),
    )

    // --------------------------------------------------------------------------

    class SimpleResult(val totalDPR: Int, val attacks: List<List<String>>) {
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

    // --------------------------------------------------------------------------

    @Test
    @EnabledIfSystemProperty(named = "RunSlowTests", matches = "true")
    fun bestMeleeDprLevel3() {
        assertEquals (SimpleResult(70, hunterBestMelee), bestDPR(3, hunterPlan, MELEE_RANGE), "hunter3")
        assertEquals (SimpleResult(72, gsBestMeleeL3),   bestDPR(3, gsPlan, MELEE_RANGE), "gs3")
        assertEquals (SimpleResult(72, wwBestMelee),     bestDPR(3, wwPlan, MELEE_RANGE), "ww3")
        assertEquals (SimpleResult(72, wwBestMelee),     bestDPR(3, wwCSPlan, MELEE_RANGE), "wwCS3")
    }

    @Test
    @EnabledIfSystemProperty(named = "RunSlowTests", matches = "true")
    fun bestRangeDprLevel3() {
        assertEquals (SimpleResult(59, hunterBestRangeL3),  bestDPR(3, hunterPlan, MELEE_RANGE*2), "hunter3")
        assertEquals (SimpleResult(63, gsBestRangeL3),      bestDPR(3, gsPlan, MELEE_RANGE*2), "gs3")
        assertEquals (SimpleResult(62, wwBestRangeL3),      bestDPR(3, wwPlan, MELEE_RANGE*2), "ww3")
        assertEquals (SimpleResult(62, wwBestRangeL3),      bestDPR(3, wwCSPlan, MELEE_RANGE*2), "wwCS3")
    }

    // --------------------------------------------------------------------------

    @Test
    @EnabledIfSystemProperty(named = "RunSlowTests", matches = "true")
    fun bestMeleeDprLevel4() {
        assertEquals (SimpleResult(78, hunterBestMelee), bestDPR(4, hunterPlan, MELEE_RANGE), "hunter4")
        assertEquals (SimpleResult(78, gsBestMeleeL4),   bestDPR(4, gsPlan, MELEE_RANGE), "gs4")
        assertEquals (SimpleResult(72, wwBestMelee),     bestDPR(4, wwPlan, MELEE_RANGE), "ww4")
        assertEquals (SimpleResult(81, wwBestMelee),     bestDPR(4, wwCSPlan, MELEE_RANGE), "wwCS4")
    }

    @Test
    @EnabledIfSystemProperty(named = "RunSlowTests", matches = "true")
    fun bestRangeDprLevel4() {
        assertEquals (SimpleResult(66, hunterBestRangeL3),  bestDPR(4, hunterPlan, MELEE_RANGE*2), "hunter4")
        assertEquals (SimpleResult(70, gsBestRangeL4),      bestDPR(4, gsPlan, MELEE_RANGE*2), "gs4")
        assertEquals (SimpleResult(62, wwBestRangeL4),      bestDPR(4, wwPlan, MELEE_RANGE*2), "ww4")
        assertEquals (SimpleResult(69, wwCsBestRangeL4),    bestDPR(4, wwCSPlan, MELEE_RANGE*2), "wwCS4")
    }

    // --------------------------------------------------------------------------

    @Test
    @EnabledIfSystemProperty(named = "RunSlowTests", matches = "true")
    fun bestMeleeDprLevel5() {
        // big jump in DPR vs Level 4, thanks to extra attack
        assertEquals (SimpleResult(134, hunterBestMeleeL5), bestDPR(5, hunterPlan, MELEE_RANGE), "hunter5")
        assertEquals (SimpleResult(127, gsBestMeleeL5),     bestDPR(5, gsPlan, MELEE_RANGE), "gs5")
        assertEquals (SimpleResult(121, wwBestMeleeL5),     bestDPR(5, wwPlan, MELEE_RANGE), "ww5")
        assertEquals (SimpleResult(136, wwBestMeleeL5),     bestDPR(5, wwCSPlan, MELEE_RANGE), "wwCS5")
    }

    @Test
    @EnabledIfSystemProperty(named = "RunSlowTests", matches = "true")
    fun bestRangeDprLevel5() {
        // big jump in DPR vs Level 4, thanks to extra attack
        assertEquals (SimpleResult(125, hunterBestRangeL5), bestDPR(5, hunterPlan, MELEE_RANGE*2), "hunter5")
        assertEquals (SimpleResult(121, gsBestRangeL5),     bestDPR(5, gsPlan, MELEE_RANGE*2), "gs5")
        assertEquals (SimpleResult(131, wwBestRangeL5),     bestDPR(5, wwPlan, MELEE_RANGE*2), "ww5")
        assertEquals (SimpleResult(141, wwBestRangeL5),     bestDPR(5, wwCSPlan, MELEE_RANGE*2), "wwCS5")
    }

    // --------------------------------------------------------------------------

    @Test
    @EnabledIfSystemProperty(named = "RunSlowTests", matches = "true")
    fun bestMeleeDprLevel8() {
        assertEquals (SimpleResult(134, hunterBestMeleeL5), bestDPR(8, hunterPlan, MELEE_RANGE), "hunter8")
        assertEquals (SimpleResult(134, gsBestMeleeL8),     bestDPR(8, gsPlan, MELEE_RANGE), "gs8")
        assertEquals (SimpleResult(121, wwBestMeleeL5),     bestDPR(8, wwPlan, MELEE_RANGE), "ww8")
        assertEquals (SimpleResult(136, wwBestMeleeL5),     bestDPR(8, wwCSPlan, MELEE_RANGE), "wwCS8")
    }

    @Test
    @EnabledIfSystemProperty(named = "RunSlowTests", matches = "true")
    fun bestRangeDprLevel8() {
        assertEquals (SimpleResult(126, hunterBestRangeL5), bestDPR(8, hunterPlan, MELEE_RANGE*2), "hunter8")
        assertEquals (SimpleResult(127, gsBestRangeL8),     bestDPR(8, gsPlan, MELEE_RANGE*2), "gs8")
        assertEquals (SimpleResult(132, wwBestRangeL5),     bestDPR(8, wwPlan, MELEE_RANGE*2), "ww8")
        assertEquals (SimpleResult(142, wwBestRangeL5),     bestDPR(8, wwCSPlan, MELEE_RANGE*2), "wwCS8")
    }

    // --------------------------------------------------------------------------

    @Test
    @EnabledIfSystemProperty(named = "RunSlowTests", matches = "true")
    fun bestMeleeDprLevel9() {
        assertEquals (SimpleResult(140, hunterBestMeleeL5), bestDPR(9, hunterPlan, MELEE_RANGE), "hunter9")
        assertEquals (SimpleResult(142, gsBestMeleeL8),     bestDPR(9, gsPlan, MELEE_RANGE), "gs9")
        assertEquals (SimpleResult(128, wwBestMeleeL5),     bestDPR(9, wwPlan, MELEE_RANGE), "ww9")
        assertEquals (SimpleResult(143, wwBestMeleeL5),     bestDPR(9, wwCSPlan, MELEE_RANGE), "wwCS9")
    }

    @Test
    @EnabledIfSystemProperty(named = "RunSlowTests", matches = "true")
    fun bestRangeDprLevel9() {
        // 30 to 40 point jump over rangeDprLevel8, thanks to "Conjure Animals" spell
        assertEquals (SimpleResult(157, hunterBestRangeL9),  bestDPR(9, hunterPlan, MELEE_RANGE*2), "hunter9")
        assertEquals (SimpleResult(167, gsBestRangeL9),      bestDPR(9, gsPlan, MELEE_RANGE*2), "gs9")
        assertEquals (SimpleResult(152, wwBestRangeL9),      bestDPR(9, wwPlan, MELEE_RANGE*2), "ww9")
        assertEquals (SimpleResult(158, wwBestRangeL9),      bestDPR(9, wwCSPlan, MELEE_RANGE*2), "wwCS9")
    }

    // --------------------------------------------------------------------------

    @Test
    @EnabledIfSystemProperty(named = "RunSlowTests", matches = "true")
    fun bestMeleeDprLevel12() {
        assertEquals (SimpleResult(140, hunterBestMeleeL5), bestDPR(12, hunterPlan, MELEE_RANGE), "hunter12")
        assertEquals (SimpleResult(148, gsBestMeleeL12),    bestDPR(12, gsPlan, MELEE_RANGE), "gs12")
        assertEquals (SimpleResult(128, wwBestMeleeL5),     bestDPR(12, wwPlan, MELEE_RANGE), "ww12")
        assertEquals (SimpleResult(143, wwBestMeleeL5),     bestDPR(12, wwCSPlan, MELEE_RANGE), "wwCS12")
    }

    @Test
    @EnabledIfSystemProperty(named = "RunSlowTests", matches = "true")
    fun bestRangeDprLevel12() {
        assertEquals (SimpleResult(162, hunterBestRangeL9),  bestDPR(12, hunterPlan, MELEE_RANGE*2), "hunter12")
        assertEquals (SimpleResult(172, gsBestRangeL12),     bestDPR(12, gsPlan, MELEE_RANGE*2), "gs12")
        assertEquals (SimpleResult(157, wwBestRangeL9),      bestDPR(12, wwPlan, MELEE_RANGE*2), "ww12")
        assertEquals (SimpleResult(163, wwCsBestRangeL12),   bestDPR(12, wwCSPlan, MELEE_RANGE*2), "wwCS12")
    }

    // --------------------------------------------------------------------------

    @Test
    @EnabledIfSystemProperty(named = "RunSlowTests", matches = "true")
    fun bestMeleeDprLevel13() {
        // big jump thanks to Conjure Woodland Beings
        assertEquals (SimpleResult(194, hunterBestMeleeL13), bestDPR(13, hunterPlan, MELEE_RANGE), "hunter13")
        assertEquals (SimpleResult(206, gsBestMeleeL13),     bestDPR(13, gsPlan, MELEE_RANGE), "gs13")
        assertEquals (SimpleResult(189, wwBestMeleeL13),     bestDPR(13, wwPlan, MELEE_RANGE), "ww13")
        assertEquals (SimpleResult(194, wwBestMeleeL13),     bestDPR(13, wwCSPlan, MELEE_RANGE), "wwCS13")
    }

    @Test
    @EnabledIfSystemProperty(named = "RunSlowTests", matches = "true")
    fun bestRangeDprLevel13() {
        assertEquals (SimpleResult(171, hunterBestRangeL13), bestDPR(13, hunterPlan, MELEE_RANGE*2), "hunter13")
        assertEquals (SimpleResult(182, gsBestRangeL13),     bestDPR(13, gsPlan, MELEE_RANGE*2), "gs13")
        assertEquals (SimpleResult(165, wwBestRangeL9),      bestDPR(13, wwPlan, MELEE_RANGE*2), "ww13")
        assertEquals (SimpleResult(172, wwBestRangeL9),      bestDPR(13, wwCSPlan, MELEE_RANGE*2), "wwCS13")
    }
}