package com.vikinghelmet.dnd.dpr.character

import com.vikinghelmet.dnd.dpr.character.TestUtil.gs
import com.vikinghelmet.dnd.dpr.character.TestUtil.hunter
import com.vikinghelmet.dnd.dpr.character.TestUtil.ww
import com.vikinghelmet.dnd.dpr.util.Globals
import junit.framework.TestCase.assertEquals
import kotlin.test.Test

class SubclassTest {

    @Test
    fun getSubclass() {
        assertEquals("Winter Walker", ww.getSubclassName())
        assertEquals("Gloom Stalker", gs.getSubclassName())
        assertEquals("Hunter", hunter.getSubclassName())
    }

    @Test
    fun getLevelTest() {
        listOf(ww,gs,hunter).forEach { assertEquals(3, it.getLevel()) }
    }

    @Test
    fun getSubclassSpellsPrepared() {
//        TestUtil.init()
        assertEquals(0, Globals.getSubclassSpellsPrepared(hunter.getSubclassName()!!).size)

        assertEquals(mapOf(
                3 to listOf("Disguise Self"),
                5 to listOf("Rope Trick"),
                9 to listOf("Fear"),
                13 to listOf("Greater Invisibility"),
                17 to listOf("Seeming"),
            ),
            Globals.getSubclassSpellsPrepared(gs.getSubclassName()!!).associate { it.level to it.spells }
        )

        assertEquals(mapOf(
            3 to listOf("Ice Knife"),
            5 to listOf("Hold Person"),
            9 to listOf("Remove Curse"),
            13 to listOf("Ice Storm"),
            17 to listOf("Cone of Cold"),
        ),
            Globals.getSubclassSpellsPrepared(ww.getSubclassName()!!).associate { it.level to it.spells }
        )
    }
}