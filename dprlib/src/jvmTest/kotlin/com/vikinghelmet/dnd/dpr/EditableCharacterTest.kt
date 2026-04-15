package com.vikinghelmet.dnd.dpr

import com.vikinghelmet.dnd.dpr.character.feats.Feat
import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import junit.framework.TestCase.assertEquals
import kotlin.test.Test
import kotlin.test.assertTrue

class EditableCharacterTest {
    val allRangerSubclasses = listOf(TestUtil.hunterPlan, TestUtil.gsPlan, TestUtil.wwPlan, TestUtil.wwCSPlan)

    @Test
    fun getNameTest() {
        assertEquals("Leif Lightfoot", TestUtil.leifPlan.getName())
    }

    @Test
    fun level3() {
        // baseline: all stats the same at level 3
        listOf(TestUtil.hunterPlan, TestUtil.gsPlan, TestUtil.wwPlan, TestUtil.wwCSPlan).forEach {
            it.editableFields.level = 3
            assertEquals(19, it.getModifiedAbilityScore(AbilityType.Dexterity))
            assertEquals(14, it.getModifiedAbilityScore(AbilityType.Wisdom))
            assertEquals(12, it.getModifiedAbilityScore(AbilityType.Strength))
            assertEquals(listOf(Feat.Archery), it.getFeatList())
        }
    }

    @Test
    fun level4() {
        // first ASI bump occurs at level 4
        allRangerSubclasses.forEach { it.editableFields.level = 4 }

        // the hunter prioritizes Dex
        listOf(TestUtil.hunterPlan).forEach {
            assertTrue(
                it.getFeatList().contains(Feat.Sharpshooter)
            ) // some unique benefits, and a single point bump in Dex
            assertEquals(20, it.getModifiedAbilityScore(AbilityType.Dexterity)) // +1
        }

        // GS and WW prioritize Wis over Dex
        listOf(TestUtil.gsPlan, TestUtil.wwPlan).forEach {
            assertEquals(16, it.getModifiedAbilityScore(AbilityType.Wisdom))    // +2
        }

        // WW CS = the CS feat confers some unique benefits, but only a single point bump in Wis
        listOf(TestUtil.wwCSPlan).forEach {
            assertTrue(it.getFeatList().contains(Feat.ColdCaster))
            assertEquals(15, it.getModifiedAbilityScore(AbilityType.Wisdom))    // +1
        }
    }

    @Test
    fun level8() {
        allRangerSubclasses.forEach { it.editableFields.level = 8 }

        listOf(TestUtil.hunterPlan).forEach {
            assertEquals(16, it.getModifiedAbilityScore(AbilityType.Wisdom))    // +2
        }

        // GS and WW prioritize Wis over Dex
        listOf(TestUtil.gsPlan, TestUtil.wwPlan).forEach {
            assertEquals(18, it.getModifiedAbilityScore(AbilityType.Wisdom))    // +2
        }

        // For WW CS, since both Dex and Wis were odd numbers, add 1 to both (ability benefits come with even numbers)
        listOf(TestUtil.wwCSPlan).forEach {
            assertEquals(16, it.getModifiedAbilityScore(AbilityType.Wisdom))    // +1
            assertEquals(20, it.getModifiedAbilityScore(AbilityType.Dexterity)) // +1
        }
    }

    @Test
    fun level12() {
        allRangerSubclasses.forEach { it.editableFields.level = 12 }

        listOf(TestUtil.hunterPlan, TestUtil.wwCSPlan).forEach {
            assertEquals(18, it.getModifiedAbilityScore(AbilityType.Wisdom))    // +2
        }

        listOf(TestUtil.gsPlan, TestUtil.wwPlan).forEach {
            assertEquals(20, it.getModifiedAbilityScore(AbilityType.Wisdom))    // +2
        }
    }

    @Test
    fun level16() {
        allRangerSubclasses.forEach { it.editableFields.level = 16 }

        listOf(TestUtil.hunterPlan).forEach {
            assertTrue(it.getFeatList().contains(Feat.Piercer))
            assertEquals(13, it.getModifiedAbilityScore(AbilityType.Strength))    // +1
        }

        listOf(TestUtil.gsPlan, TestUtil.wwPlan).forEach {
            assertTrue(it.getFeatList().contains(Feat.Piercer))
            assertEquals(20, it.getModifiedAbilityScore(AbilityType.Dexterity))   // +1
        }

        listOf(TestUtil.wwCSPlan).forEach {
            assertEquals(20, it.getModifiedAbilityScore(AbilityType.Wisdom))    // +2
        }
    }
}