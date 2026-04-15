package com.vikinghelmet.dnd.dpr

import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import junit.framework.TestCase.assertEquals
import kotlin.test.Test

class EditableCharacterTest {
    @Test
    fun getNameTest() {
        assertEquals("Leif Lightfoot", TestUtil.leifPlan.getName())
    }

    @Test
    fun asiPlan() {
        // baseline: all stats the same at level 3

        listOf(TestUtil.hunterPlan, TestUtil.gsPlan, TestUtil.wwPlan, TestUtil.wwCSPlan).forEach {
            it.editableFields.level = 3
            assertEquals(19, it.getModifiedAbilityScore(AbilityType.Dexterity))
            assertEquals(14, it.getModifiedAbilityScore(AbilityType.Wisdom))
        }

        // first bump occurs at level 4 ... the hunter prioritizes Dex
        listOf(TestUtil.hunterPlan).forEach {
            it.editableFields.level = 4
            assertEquals(20, it.getModifiedAbilityScore(AbilityType.Dexterity))
        }

        // GS and WW prioritize Wis over Dex
        listOf(TestUtil.gsPlan, TestUtil.wwPlan).forEach {
            it.editableFields.level = 4
            assertEquals(16, it.getModifiedAbilityScore(AbilityType.Wisdom))
        }

        // WW CS = WW + ColdCaster Feat; this gives us a single point bump in Wis (along with a few other features)
        listOf(TestUtil.wwCSPlan).forEach {
            // the hunter prioritizes Dex; other subclasses prioritize Wis
            it.editableFields.level = 4
            assertEquals(15, it.getModifiedAbilityScore(AbilityType.Wisdom))
        }
    }
}