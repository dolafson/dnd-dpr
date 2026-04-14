package com.vikinghelmet.dnd.dpr

import com.vikinghelmet.dnd.dpr.scenario.EffectManager
import com.vikinghelmet.dnd.dpr.scenario.Scenario
import com.vikinghelmet.dnd.dpr.scenario.ScenarioBuilder
import com.vikinghelmet.dnd.dpr.scenario.ScenarioCalculator
import com.vikinghelmet.dnd.dpr.turn.ActionCalculator
import com.vikinghelmet.dnd.dpr.turn.Attack
import com.vikinghelmet.dnd.dpr.turn.AvgMinMax
import com.vikinghelmet.dnd.dpr.turn.Turn
import com.vikinghelmet.dnd.dpr.util.Constants
import com.vikinghelmet.dnd.dpr.util.Constants.DEFAULT_NUM_TARGETS
import com.vikinghelmet.dnd.dpr.util.Constants.DEFAULT_TARGET_RADIUS
import com.vikinghelmet.dnd.dpr.util.Globals
import kotlin.test.Test
import kotlin.test.assertEquals

class ActionCalculatorTest {

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

    @Test
    fun melee() {
        val character = TestUtil.leif
        val weapon   = character.getWeapon("Shortsword")
        val turns    = listOf(Turn(listOf(Attack (Globals.getMonster("Goblin"), weapon))))
        val scenario = Scenario(character, turns, DEFAULT_NUM_TARGETS, DEFAULT_TARGET_RADIUS)

        // calculate single action via action calculator
        val actionCalculator = ActionCalculator(scenario, EffectManager(ArrayList()))
        var attackResult = actionCalculator.getMeleeOrRangeDPR(weapon, turns[0].attacks[0], 1, 1, 1)

        assertEquals (AvgMinMax(0.6f, 0.36f, 0.84f, 0.6f), attackResult.chanceToHit)
        assertEquals (AvgMinMax(4.68f, 2.71f, 6.64f, 4.68f), attackResult.damagePerRound)

        // calculate single turn via scenario calculator
        val attackResultList = ScenarioCalculator(scenario).calculateDPR(1, 1, turns[0].attacks[0], actionCalculator)

        attackResult = attackResultList[0]
        assertEquals (AvgMinMax(0.6f, 0.36f, 0.84f, 0.6f), attackResult.chanceToHit)
        assertEquals (AvgMinMax(4.68f, 2.71f, 6.64f, 4.68f), attackResult.damagePerRound)

        // calculate entire scenario via scenario calculator
        val scenarioResult = ScenarioCalculator(scenario).calculateDPRForAllTurns()

        attackResult = scenarioResult.attackResults[0]
        assertEquals (AvgMinMax(0.6f, 0.36f, 0.84f, 0.6f), attackResult.chanceToHit)
        assertEquals (AvgMinMax(4.68f, 2.71f, 6.64f, 4.68f), attackResult.damagePerRound)
    }

    @Test
    fun range() {
        val character = TestUtil.leif
        val weapon = character.getWeapon("Longbow")
        val turns = listOf(Turn(listOf(Attack(Globals.getMonster("Goblin"), weapon))))
        val scenario = Scenario(character, turns, DEFAULT_NUM_TARGETS, DEFAULT_TARGET_RADIUS)

        // calculate single action via action calculator
        val actionCalculator = ActionCalculator(scenario, EffectManager(ArrayList()))
        var attackResult = actionCalculator.getMeleeOrRangeDPR(weapon, turns[0].attacks[0], 1, 1, 1)

        println ("attackResult.chanceToHit    = ${attackResult.chanceToHit.toFullString()}")
        println ("attackResult.damagePerRound = ${attackResult.damagePerRound.toFullString()}")

        // character has Archery Fighting Style, which gives +2 to range attack
        // this yields higher %hit on Longbow vs Shortsword above
        assertEquals(AvgMinMax(0.7f, 0.49f, 0.91f, 0.7f), attackResult.chanceToHit)

        // Longbow is d8, while shortsword is d6; that combined with higher %hit (above) yields higher DPR
        assertEquals(AvgMinMax(6.18f, 4.18f, 8.17f, 6.18f), attackResult.damagePerRound)
    }


    @Test
    fun bonusAction() {
        val character = TestUtil.leif
        val weapon   = character.getWeapon("Shortsword")
        val turns    = listOf(Turn(listOf(Attack (Globals.getMonster("Goblin"), weapon, isBonusAction = true))))
        val scenario = Scenario(character, turns, DEFAULT_NUM_TARGETS, DEFAULT_TARGET_RADIUS)

        // calculate single action via action calculator
        val actionCalculator = ActionCalculator(scenario, EffectManager(ArrayList()))
        var attackResult = actionCalculator.getMeleeOrRangeDPR(weapon, turns[0].attacks[0], 1, 1, 1)

        // bonus action has the same odds of hitting, but lower damage (you can't add PB to BA damage)
        assertEquals (AvgMinMax(0.6f, 0.36f, 0.84f, 0.6f), attackResult.chanceToHit)
        assertEquals (AvgMinMax(2.28f, 1.27f, 3.28f, 2.28f), attackResult.damagePerRound)
    }

    @Test
    fun spellAttackSaveForHalf() {
        val character = TestUtil.leif
        val spell    = Globals.getSpell("Hail of Thorns", character.is2014())
        val turns    = listOf(Turn(listOf(Attack (Globals.getMonster("Goblin"), spell, isBonusAction = true))))
        val scenario = Scenario(character, turns, DEFAULT_NUM_TARGETS, DEFAULT_TARGET_RADIUS)

        val actionCalculator = ActionCalculator(scenario, EffectManager(ArrayList()))
        var attackResult = actionCalculator.getSavingThrowSpellDPR (spell.getSpellAttacks()[0], spell, turns[0].attacks[0])

        assertEquals (AvgMinMax(0.45f, 0.2f, 0.7f, 0.45f), attackResult.chanceToHit)
        assertEquals (AvgMinMax(3.85f, 3.11f, 4.59f, 3.85f), attackResult.damagePerRound)
    }

    @Test
    fun rangeSpellAttack() {
        val character = TestUtil.eldir
        val spell    = Globals.getSpell("Fire Bolt", character.is2014())
        val turns    = listOf(Turn(listOf(Attack (Globals.getMonster("Goblin"), spell, isBonusAction = true))))
        val scenario = Scenario(character, turns, DEFAULT_NUM_TARGETS, DEFAULT_TARGET_RADIUS)

        val actionCalculator = ActionCalculator(scenario, EffectManager(ArrayList()))
        var attackResult = actionCalculator.getMeleeOrRangeDPR(spell.getSpellAttacks()[0], turns[0].attacks[0], 1, 1, 1)

        assertEquals (AvgMinMax(0.6f, 0.36f, 0.84f, 0.6f), attackResult.chanceToHit)
        assertEquals (AvgMinMax(3.58f, 1.99f, 5.16f, 3.58f), attackResult.damagePerRound)
    }

}