package com.vikinghelmet.dnd.dpr

import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import com.vikinghelmet.dnd.dpr.scenario.ScenarioBuilder
import com.vikinghelmet.dnd.dpr.util.Constants
import com.vikinghelmet.dnd.dpr.util.Constants.DEFAULT_NUM_TARGETS
import com.vikinghelmet.dnd.dpr.util.Constants.DEFAULT_TARGET_SPACING
import com.vikinghelmet.dnd.dpr.util.Globals
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class MonsterTest {

    @Test
    fun getGoblin() {
        TestUtil.dependency()
        val goblin = Globals.getMonster("Goblin")
        assertNotNull (goblin)
        assertEquals (15, goblin.getAC())
        assertFalse (goblin.isEvasive())

        val weaponList = goblin.getWeaponList()
        assertEquals(weaponList.size, 2)

        val scimitar = weaponList.firstOrNull { it.name == "Scimitar" }
        assertNotNull (scimitar)
        assertEquals("1d6", scimitar!!.damage)
        assertEquals(2, scimitar.flatBonusDamage)
        assertEquals(1, scimitar.attackType)

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

    @Test
    fun turnOptions() {
        TestUtil.dependency()

        val goblin = Globals.getMonster("Goblin")
        val character = TestUtil.leif
        val meleeBuilder = ScenarioBuilder(goblin, character)
        val meleeScenarios = meleeBuilder.build(Constants.MELEE_RANGE, Constants.NUM_TURNS_PER_SCENARIO, DEFAULT_NUM_TARGETS, DEFAULT_TARGET_SPACING)

        assertEquals(1, meleeBuilder.turnOptions.size)
        assertEquals(1, meleeScenarios.size)

//        println("attacks = ${ meleeBuilder.turnOptions.map { it.attacks.map { it2 -> it2.getLabel()} } }")
        assertEquals(listOf(
            listOf("Scimitar"),
        ),  meleeBuilder.turnOptions.map { it.attacks.map { it2 -> it2.getLabel()} } )


        val rangeBuilder = ScenarioBuilder(goblin, character)
        val rangeScenarios = rangeBuilder.build(60, Constants.NUM_TURNS_PER_SCENARIO, DEFAULT_NUM_TARGETS, DEFAULT_TARGET_SPACING)

        rangeBuilder.turnOptions.forEach {
            val buf = StringBuilder()
            it.attacks.forEach { buf.append(it.getLabel()).append(",") }
            println("turn option = { $buf }")
        }

        assertEquals(1, rangeBuilder.turnOptions.size)
        assertEquals(1, rangeScenarios.size)

        assertEquals(listOf(
            listOf("Shortbow"),
        ),  rangeBuilder.turnOptions.map { it.attacks.map { it2 -> it2.getLabel()} } )

    }
}