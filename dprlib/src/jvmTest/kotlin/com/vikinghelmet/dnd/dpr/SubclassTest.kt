package com.vikinghelmet.dnd.dpr

import com.vikinghelmet.dnd.dpr.util.Globals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SubclassTest {

    @Test
    fun getSubclass() {
        assertEquals("Winter Walker", TestUtil.ww.getSubclassName())
        assertEquals("Gloom Stalker", TestUtil.gs.getSubclassName())
        assertEquals("Hunter", TestUtil.hunter.getSubclassName())
    }

    @Test
    fun getLevelTest() {
        listOf(TestUtil.ww, TestUtil.gs, TestUtil.hunter).forEach { assertEquals(3, it.getLevel()) }
    }

    @Test
    fun getSubclassSpellsPrepared() {
//        TestUtil.init()
        assertEquals(0, Globals.getSubclassSpellsPrepared(TestUtil.hunter.getSubclassName()!!).size)

        assertEquals(mapOf(
                3 to listOf("Disguise Self"),
                5 to listOf("Rope Trick"),
                9 to listOf("Fear"),
                13 to listOf("Greater Invisibility"),
                17 to listOf("Seeming"),
            ),
            Globals.getSubclassSpellsPrepared(TestUtil.gs.getSubclassName()!!).associate { it.level to it.spells }
        )

        assertEquals(mapOf(
            3 to listOf("Ice Knife"),
            5 to listOf("Hold Person"),
            9 to listOf("Remove Curse"),
            13 to listOf("Ice Storm"),
            17 to listOf("Cone of Cold"),
        ),
            Globals.getSubclassSpellsPrepared(TestUtil.ww.getSubclassName()!!).associate { it.level to it.spells }
        )
    }
}