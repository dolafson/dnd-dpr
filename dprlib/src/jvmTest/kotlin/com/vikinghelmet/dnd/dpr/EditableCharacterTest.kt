package com.vikinghelmet.dnd.dpr

import com.vikinghelmet.dnd.dpr.TestUtil.gsPlan
import com.vikinghelmet.dnd.dpr.TestUtil.hunterPlan
import com.vikinghelmet.dnd.dpr.TestUtil.wwCSPlan
import com.vikinghelmet.dnd.dpr.TestUtil.wwPlan
import com.vikinghelmet.dnd.dpr.character.feats.Feat
import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import dev.shivathapaa.logger.api.LoggerFactory
import kotlinx.serialization.Transient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class EditableCharacterTest {
    @Transient private val logger = LoggerFactory.get(EditableCharacterTest::class.simpleName ?: "")

    val allRangerSubclasses = listOf(hunterPlan, gsPlan, wwPlan, wwCSPlan)

    @Test
    fun getNameTest() {
        assertEquals("Leif - Hunter", hunterPlan.getName())
        assertEquals("Leif - GS", gsPlan.getName())
        assertEquals("Leif - Winter Walker", wwPlan.getName())
        assertEquals("Leif - Winter Walker + Cold Caster", wwCSPlan.getName())
    }

    @Test
    fun asiLevel3() {
        // baseline: all stats the same at level 3
        listOf(hunterPlan, gsPlan, wwPlan, wwCSPlan).forEach {
            it.editableFields.level = 3
            assertEquals(19, it.getModifiedAbilityScore(AbilityType.Dexterity))
            assertEquals(14, it.getModifiedAbilityScore(AbilityType.Wisdom))
            assertEquals(12, it.getModifiedAbilityScore(AbilityType.Strength))
            assertEquals(listOf(Feat.Archery), it.getFeatList())
        }
    }

    @Test
    fun asiLevel4() {
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
    fun asiLevel8() {
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
    fun asiLevel12() {
        allRangerSubclasses.forEach { it.editableFields.level = 12 }

        listOf(hunterPlan, wwCSPlan).forEach {
            assertEquals(18, it.getModifiedAbilityScore(AbilityType.Wisdom))    // +2
        }

        listOf(gsPlan, wwPlan).forEach {
            assertEquals(20, it.getModifiedAbilityScore(AbilityType.Wisdom))    // +2
        }
    }

    @Test
    fun asiLevel16() {
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