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
        TestUtil.hunterPlan.editableFields.level = 3
        assertEquals(19, TestUtil.hunterPlan.getModifiedAbilityScore(AbilityType.Dexterity))

        TestUtil.hunterPlan.editableFields.level = 4
        assertEquals(20, TestUtil.hunterPlan.getModifiedAbilityScore(AbilityType.Dexterity))
    }
}