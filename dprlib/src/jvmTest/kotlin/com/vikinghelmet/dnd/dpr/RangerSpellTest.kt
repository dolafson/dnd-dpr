package com.vikinghelmet.dnd.dpr

import com.vikinghelmet.dnd.dpr.character.actions.ActionModifier
import com.vikinghelmet.dnd.dpr.scenario.EffectManager
import com.vikinghelmet.dnd.dpr.scenario.Scenario
import com.vikinghelmet.dnd.dpr.scenario.ScenarioCalculator
import com.vikinghelmet.dnd.dpr.turn.ActionCalculator
import com.vikinghelmet.dnd.dpr.turn.Attack
import com.vikinghelmet.dnd.dpr.turn.Turn
import com.vikinghelmet.dnd.dpr.util.Constants.DEFAULT_TARGET_SPACING
import com.vikinghelmet.dnd.dpr.util.Globals
import dev.shivathapaa.logger.api.LogLevel
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class RangerSpellTest {

    @Test
    fun steelWindScenario() {
        val character = TestUtil.gsDexPlan
        character.editableFields.level = 17
        val conjureBarrage  = Globals.getSpell("Conjure Barrage", character.is2014())
        val conjureWoodlandBeings  = Globals.getSpell("Conjure Woodland Beings", character.is2014())
        val steelWindStrike = Globals.getSpell("Steel Wind Strike", character.is2014())
        val monster  = Globals.getMonster("Goblin")
        val weapon = character.getWeapon("Shortsword")

        val conjureBarrageTurn = Turn(listOf(Attack(monster, conjureBarrage)))
        val dreadAttack = Attack (monster, weapon, actionModifiers = mutableListOf(ActionModifier.DreadfulStrike))
        val swordAttack = Attack (monster, weapon)

        val fiveTurns =
                //listOf(conjureBarrageTurn) +
                listOf(Turn(listOf(Attack(monster, steelWindStrike)))) +

                listOf(Turn(listOf(dreadAttack, swordAttack, swordAttack))) +
                List(2) { conjureBarrageTurn } +
                listOf(Turn(listOf(Attack(monster, conjureWoodlandBeings))))

        var scenario = Scenario(character, fiveTurns, 4, DEFAULT_TARGET_SPACING)
        var result   = SimpleResult(ScenarioCalculator(scenario).calculateDPRForAllTurns())

//        println("result = ${SimpleResult(result)}")
        assertEquals(428, result.totalDamage)
    }


    @Test
    fun steelWindAttack() {
        val character = TestUtil.gsDexPlan
        character.editableFields.level = 17

        Globals.initLogger(LogLevel.DEBUG)

        val spell    = Globals.getSpell("Steel Wind Strike", character.is2014())
        val turns    = listOf(Turn(listOf(Attack (Globals.getMonster("Goblin"), spell))))

        val scenario = Scenario(character, turns, 4, DEFAULT_TARGET_SPACING)
        val actionCalculator = ActionCalculator(scenario, EffectManager(ArrayList()))

        val result = actionCalculator.getMeleeOrRangeDPR(spell.getSpellAttacks()[0], turns[0].attacks[0], 1, 1, 1)
        println(result)

        assertEquals(0.85f,  Globals.round2(result.chanceToHit.final))
        assertEquals(118.8f,    Globals.round2(result.damagePerRound.final))
    }

    @Test
    fun coneOfCold() {
        //val character = TestUtil.wwPlan
        val character = TestUtil.wwCCPlan
        character.editableFields.level = 17
        /*
  { "level": 16, "totalDamage": 386, "attacks": [ ... ice storm
  { "level": 17, "totalDamage": 496, "attacks": [ ... cone of cold ... less for wwCC ?
         */
        val conjureBarrage  = Globals.getSpell("Conjure Barrage", character.is2014())
        val conjureWoodlandBeings  = Globals.getSpell("Conjure Woodland Beings", character.is2014())
        val iceStorm   = Globals.getSpell("Ice Storm", character.is2014())
        val coneOfCold = Globals.getSpell("Cone of Cold", character.is2014())
        val monster  = Globals.getMonster("Goblin")

        val conjureBarrageTurn = Turn(listOf(Attack(monster, conjureBarrage)))

        val fiveTurns =
            List(3) { conjureBarrageTurn } +
            listOf(Turn(listOf(Attack(monster, conjureWoodlandBeings)))) +

            //listOf(Turn(listOf(Attack(monster, iceStorm))))
            listOf(Turn(listOf(Attack(monster, coneOfCold))))

        var scenario = Scenario(character, fiveTurns, 4, DEFAULT_TARGET_SPACING)
        var result   = ScenarioCalculator(scenario).calculateDPRForAllTurns()

        println("result = ${SimpleResult(result)}")
        assertEquals(496, SimpleResult(result).totalDamage)
    }
}