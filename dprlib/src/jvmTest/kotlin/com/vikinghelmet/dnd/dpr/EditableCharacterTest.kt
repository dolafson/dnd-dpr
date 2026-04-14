package com.vikinghelmet.dnd.dpr

import junit.framework.TestCase.assertEquals
import kotlin.test.Test

class EditableCharacterTest {
    @Test
    fun getNameTest() {
        assertEquals("Leif Lightfoot", TestUtil.leifPlan.getName())
    }
}