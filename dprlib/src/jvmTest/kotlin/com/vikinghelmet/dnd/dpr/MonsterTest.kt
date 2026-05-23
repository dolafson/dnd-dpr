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
        assertNotNull(goblin)
        assertEquals(15, goblin.getAC())
        assertFalse(goblin.isEvasive())

        val weaponList = goblin.getWeaponList()
        assertEquals(weaponList.size, 2)

        val scimitar = weaponList.firstOrNull { it.name == "Scimitar" }
        assertNotNull(scimitar)
        assertEquals("1d6", scimitar!!.getDamageList().first().dice.toString())
        assertEquals(2, scimitar.getDamageList().first().bonus)
        assertEquals(1, scimitar.attackType)

        // verify goblin ability mods; their best ability is Dex
        assertEquals(
            listOf(-1, 2, 0, 0, -1, -1),
            AbilityType.getAllNotALL().map { goblin.getAbilityModifier(it) }.toList()
        )
    }

    @Test
    fun globalTest() {
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

    @Test
    fun dragonDamage() {
        TestUtil.dependency()
        val dragon = Globals.getMonster("Young Green Dragon")
        assertNotNull(dragon)
        assertEquals(18, dragon.getAC())
        assertFalse(dragon.isEvasive())

        assertEquals(
            listOf(4, 1, 3, 3, 1, 2),
            AbilityType.getAllNotALL().map { dragon.getAbilityModifier(it) }.toList()
        )

        val weaponList = dragon.getWeaponList()
        assertEquals(weaponList.size, 4)

        val bite = weaponList.firstOrNull { it.name == "Bite" }
        assertNotNull(bite)
        assertEquals("[2d10 + 4 piercing, 2d6 poison]", bite!!.getDamageList().toString())
        assertEquals(7, bite.getAttackBonus())
        assertEquals(1, bite.attackType)

        val claw = weaponList.firstOrNull { it.name == "Claw" }
        assertNotNull(claw)
        assertEquals("[2d6 + 4 slashing]", claw!!.getDamageList().toString())
        assertEquals(7, claw.getAttackBonus())
        assertEquals(1, claw.attackType)

        // println(weaponList)

        /*
            {
              "Name": "Multiattack",
              "Desc": "The dragon makes three attacks: one with its bite and two with its claws."
            },

            {
              "Name": "Poison Breath (Recharge 5-6)",
              "Desc": "The dragon exhales poisonous gas in a 30-foot cone. Each creature in that area must make a DC 14 Constitution saving throw, taking 42 (12d6) poison damage on a failed save, or half as much damage on a successful one."
            }
        */
        val multiAttack = weaponList.firstOrNull { it.name == "Multiattack" } // TODO: 2 claws ...
        assertNotNull(multiAttack)
        assert(multiAttack!!.getDamageList().isEmpty())

        val breath = weaponList.firstOrNull { it.name.startsWith("Poison Breath") } // TODO
        assertNotNull(breath)
        assert(breath!!.getDamageList().isEmpty())
    }

}