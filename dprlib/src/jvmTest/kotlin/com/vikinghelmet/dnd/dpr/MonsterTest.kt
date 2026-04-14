package com.vikinghelmet.dnd.dpr

import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import com.vikinghelmet.dnd.dpr.util.Globals
import junit.framework.TestCase.assertFalse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class MonsterTest {

    @Test
    fun getGoblin() {
        TestUtil.dependency()
        val goblin = Globals.getMonster("Goblin")
        assertNotNull (goblin)
        assertEquals (15, goblin.getAC())
        assertFalse (goblin.isEvasive())

        // verify goblin ability mods; their best ability is Dex
        assertEquals (listOf(-1, 2, 0, 0, -1, -1), AbilityType.getAllNotALL().map { goblin.getAbilityModifier(it) }.toList())

        // verify only one monster is "evasive" (hint: it's a player character subclass)
        assertEquals (1, Globals.monsters.count { it.isEvasive() })

        // verify max strength modifier across all monsters
        assertEquals (10, Globals.monsters.maxOf { it.getAbilityModifier(AbilityType.Strength) })

        // verify which monster is the strongest
        assertEquals (Globals.getMonster("Ancient Gold Dragon"),
            Globals.monsters.maxBy { it.getAbilityModifier(AbilityType.Strength) })
    }
}