package com.vikinghelmet.dnd.dpr.character

import com.vikinghelmet.dnd.dpr.scenario.ScenarioBuilder
import com.vikinghelmet.dnd.dpr.util.Constants
import com.vikinghelmet.dnd.dpr.util.Constants.DEFAULT_NUM_TARGETS
import com.vikinghelmet.dnd.dpr.util.Constants.DEFAULT_TARGET_RADIUS
import com.vikinghelmet.dnd.dpr.util.Globals
import kotlin.test.Test
import kotlin.test.assertEquals

class ScenarioTest {

    @Test
    fun getActionsAvailable() {
        TestUtil.dependency()
        // ScenarioBuilder(TestUtil.leif, goblin).testActionsAvailable()

        val character = TestUtil.leif

        assertEquals(mapOf(
            5 to listOf("Dagger", "Quarterstaff", "Shortsword", "Shortsword"),
            20 to listOf("Dagger"),
            60 to listOf("Mind Sliver"),
            90 to listOf("Entangle"),
            150 to listOf("Longbow"),
        ),
            character.getActionsAvailable().mapOfLists.mapValues { it.value.map { it2 -> it2.getActionName() } }
        )

        // melee actions
        assertEquals(listOf("Dagger", "Quarterstaff", "Shortsword"),
            character.getActionsAvailable().getPrimaryAction(Constants.MELEE_RANGE).map { it.getActionName()})

        // melee bonus actions
        assertEquals(listOf("Hunter's Mark"), character.getPreparedBonusActionSpells(Constants.MELEE_RANGE).map { it.name })

        // short range actions
        assertEquals(listOf("Dagger", "Mind Sliver", "Entangle", "Longbow").toSet(),
            character.getActionsAvailable().getPrimaryAction(Constants.MELEE_RANGE*2).map { it.getActionName()}.toSet())

        // short range bonus actions
        assertEquals(listOf("Hail of Thorns", "Hunter's Mark"), character.getPreparedBonusActionSpells(Constants.MELEE_RANGE*2).map { it.name })
    }

    @Test
    fun turnOptions() {
        val character = TestUtil.leif
        val meleeBuilder = ScenarioBuilder(character, Globals.getMonster("Goblin"))
        val meleeScenarios = meleeBuilder.build(Constants.MELEE_RANGE, Constants.NUM_TURNS_PER_SCENARIO, DEFAULT_NUM_TARGETS, DEFAULT_TARGET_RADIUS)

        assertEquals(7, meleeBuilder.turnOptions.size)
        assertEquals(4864, meleeScenarios.size)

        assertEquals(listOf(
                listOf("Dagger","Shortsword"),
                listOf("Dagger","Hunter's Mark"),
                listOf("Quarterstaff"),
                listOf("Quarterstaff","Hunter's Mark"),
                listOf("Shortsword","Dagger"),
                listOf("Shortsword","Shortsword"),
                listOf("Shortsword","Hunter's Mark"),
            ),  meleeBuilder.turnOptions.map { it.attacks.map { it2 -> it2.getLabel()} } )


        val rangeBuilder = ScenarioBuilder(character, Globals.getMonster("Goblin"))
        val rangeScenarios = rangeBuilder.build(60, Constants.NUM_TURNS_PER_SCENARIO, DEFAULT_NUM_TARGETS, DEFAULT_TARGET_RADIUS)

        rangeBuilder.turnOptions.forEach {
            val buf = StringBuilder()
            it.attacks.forEach { buf.append(it.getLabel()).append(",") }
            println("turn option = { $buf }")
        }

        assertEquals(5, rangeBuilder.turnOptions.size)
        assertEquals(792, rangeScenarios.size)

        assertEquals(listOf(
            listOf("Longbow"),
            listOf("Longbow","Hail of Thorns"),
            listOf("Longbow","Hunter's Mark"),
            listOf("Entangle"),
            listOf("Mind Sliver"),
        ),  rangeBuilder.turnOptions.map { it.attacks.map { it2 -> it2.getLabel()} } )

    }
}