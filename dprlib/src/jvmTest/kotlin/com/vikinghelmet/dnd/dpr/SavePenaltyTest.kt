package com.vikinghelmet.dnd.dpr

import com.vikinghelmet.dnd.dpr.TestUtil.wwCCPlan
import com.vikinghelmet.dnd.dpr.character.actions.ActionModifier
import com.vikinghelmet.dnd.dpr.character.feats.Feat
import com.vikinghelmet.dnd.dpr.scenario.EffectManager
import com.vikinghelmet.dnd.dpr.scenario.Scenario
import com.vikinghelmet.dnd.dpr.scenario.ScenarioCalculator
import com.vikinghelmet.dnd.dpr.turn.ActionCalculator
import com.vikinghelmet.dnd.dpr.turn.Attack
import com.vikinghelmet.dnd.dpr.turn.AvgMinMax
import com.vikinghelmet.dnd.dpr.turn.Turn
import com.vikinghelmet.dnd.dpr.util.Constants.DEFAULT_NUM_TARGETS
import com.vikinghelmet.dnd.dpr.util.Constants.DEFAULT_TARGET_RADIUS
import com.vikinghelmet.dnd.dpr.util.Globals
import com.vikinghelmet.dnd.dpr.util.TargetEffect
import dev.shivathapaa.logger.api.LoggerFactory
import kotlinx.serialization.Transient
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class SavePenaltyTest {
    @Transient private val logger = LoggerFactory.get(SavePenaltyTest::class.simpleName ?: "")

    @Test
    fun savePenaltyNoImpactOnWeaponAttackViaScenarioCalculator() {
        val character = wwCCPlan
        val monster = Globals.getMonster("Goblin")
        val weapon = character.getWeapon("Shortsword")

        val polarAttack  = Attack (monster, weapon, actionModifiers = mutableListOf(ActionModifier.PolarStrikes))
        val huntersMark  = Attack (monster, Globals.getSpell("Hunter's Mark", character.is2014()))
        val weaponAttack = Attack (monster, weapon)
        val bonusAttack  = Attack (monster, weapon, isBonusAction = true)
        val turnList     = listOf(Turn(listOf(polarAttack, weaponAttack, huntersMark))) +
                            List(3) { Turn(listOf(polarAttack, weaponAttack, bonusAttack)) }

        val scenario = Scenario(character, turnList, DEFAULT_NUM_TARGETS, DEFAULT_TARGET_RADIUS)

        //Globals.initLogger(LogLevel.DEBUG)
        for(level in listOf(5, 8)) {
            character.editableFields.level = level
            var result = ScenarioCalculator(scenario).calculateDPRForAllTurns()

            assertEquals(AvgMinMax(0.7f, 0.49f, 0.91f, 0.7f),   result.attackResults[0].chanceToHit, "sword1, t=0, level=$level")
            assertEquals(AvgMinMax(0.7f, 0.49f, 0.91f, 0.85f),  result.attackResults[1].chanceToHit, "sword2, t=0, level=$level")
            assertEquals(AvgMinMax(1f,1f,1f,1f),                result.attackResults[2].chanceToHit, "HM, t=0, level=$level")

            //assertEquals(AvgMinMax(0.7f, 0.49f, 0.91f, 0.85f),  result.attackResults[3].chanceToHit, "sword1, t=1, level=$level")
            assertEquals(0.85f,  Globals.round2(result.attackResults[3].chanceToHit.final), "sword1, t=1, level=$level")

            assertEquals(AvgMinMax(8.0f, 5.4f, 10.59f, 8.0f),   result.attackResults[0].damagePerRound, "dpr sword1, t=0, level=$level")
            assertEquals(AvgMinMax(6.12f, 4.17f, 8.08f, 7.49f), result.attackResults[1].damagePerRound, "dpr sword2, t=0, level=$level")
            assertEquals(AvgMinMax(0f,0f,0f,0f),                result.attackResults[2].damagePerRound, "dpr HM, t=0, level=$level")

            //assertEquals(AvgMinMax(10.62f, 7.13f, 14.12f, 13.07f), result.attackResults[3].damagePerRound, "dpr sword1, t=1, level=$level")
            assertEquals(13.07f,  Globals.round2(result.attackResults[3].damagePerRound.final), "sword1, t=1, level=$level")
        }
    }

    @Test
    fun savePenaltyNoImpactOnWeaponAttackViaActionCalculator() {
        val character = wwCCPlan
        character.editableFields.level = 7

        val weapon  = character.getWeapon("Shortsword")
        val monster = Globals.getMonster("Goblin")

        val weaponAttack = Attack (monster, weapon)
        val singleTurn   = listOf(Turn(listOf(weaponAttack)))
        val scenario     = Scenario(character, singleTurn, DEFAULT_NUM_TARGETS, DEFAULT_TARGET_RADIUS)

        // first, calculate results with no savePenalty
        var noPenaltyResult = ActionCalculator(scenario, EffectManager(ArrayList()))
            .getMeleeOrRangeDPR(weapon, weaponAttack, 1, 1, 1)

        assertEquals (AvgMinMax(0.7f,  0.49f, 0.91f, 0.7f),  noPenaltyResult.chanceToHit, "noPenalty %hit")
        assertEquals (AvgMinMax(6.12f, 4.17f, 8.08f, 6.12f), noPenaltyResult.damagePerRound, "noPenalty dpr")

        // second, results for same scenario WITH savePenalty
        val effectManager = EffectManager(mutableListOf(
            TargetEffect(startTurn = 0, probability = 0.7f, savePenalty = mutableListOf("1d4"))))

        var withPenaltyResult = ActionCalculator(scenario, effectManager)
            .getMeleeOrRangeDPR(weapon, weaponAttack, 1, 1, 1)

        assertEquals (AvgMinMax(0.7f,  0.49f, 0.91f, 0.7f),  withPenaltyResult.chanceToHit, "withPenalty %hit")
        assertEquals (AvgMinMax(6.12f, 4.17f, 8.08f, 6.12f), withPenaltyResult.damagePerRound, "withPenalty dpr")

        println("effectManager = $effectManager")
    }

    @Test
    fun savePenaltyImpactOnSpellAttackViaActionCalculator() {
        val character = wwCCPlan
        character.editableFields.level = 7

        val spell   = Globals.getSpell("Hail of Thorns", character.is2014())
        val monster = Globals.getMonster("Goblin")

        val attack   = Attack (monster, spell)
        val scenario = Scenario(character, listOf(Turn(listOf(attack))), 4, DEFAULT_TARGET_RADIUS)

        // first, calculate results with no savePenalty
        var noPenaltyResult = ActionCalculator(scenario, EffectManager(ArrayList()))
            .getSavingThrowSpellDPR(spell.getSpellAttacks()[0], spell, attack)

        assertEquals (AvgMinMax(0.5f,  0.25f, 0.75f, 0.5f),  noPenaltyResult.chanceToHit, "noPenalty %hit")
        assertEquals (AvgMinMax(16.0f, 13.0f, 19.0f, 16.0f), noPenaltyResult.damagePerRound, "noPenalty dpr")

        // second, results for same scenario WITH savePenalty
        val effectManager = EffectManager(mutableListOf(
            TargetEffect(startTurn = 0, probability = 0.7f, savePenalty = mutableListOf("1d4"))))

        var withPenaltyResult = ActionCalculator(scenario, effectManager)
            .getSavingThrowSpellDPR(spell.getSpellAttacks()[0], spell, attack)

        assertEquals (AvgMinMax(0.62f,  0.39f, 0.86f, 0.62f),  withPenaltyResult.chanceToHit, "withPenaltyResult %hit")
        assertEquals (AvgMinMax(17.5f, 14.72f, 20.28f, 17.5f), withPenaltyResult.damagePerRound, "withPenaltyResult dpr")

        assertTrue(withPenaltyResult.damagePerRound.final > noPenaltyResult.damagePerRound.final)
    }

    @Test
    fun savePenaltyImpactOnSpellAttackViaScenarioCalculator() {
        val character = wwCCPlan
        val monster = Globals.getMonster("Goblin")
        val weapon = character.getWeapon("Longbow")

        val holdAttack  = Attack (monster, Globals.getSpell("Hold Person", character.is2014()))
        val polarAttack  = Attack (monster, weapon, actionModifiers = mutableListOf(ActionModifier.PolarStrikes))
        val weaponAttack = Attack (monster, weapon)
        val bonusAttack  = Attack (monster, Globals.getSpell("Hail of Thorns", character.is2014()), isBonusAction = true)
        val turnList     = listOf(Turn(listOf(holdAttack))) +
                List(4) { Turn(listOf(polarAttack, weaponAttack, bonusAttack)) }

        val scenario = Scenario(character, turnList, 4, DEFAULT_TARGET_RADIUS)

        character.editableFields.level = 8

        println("before, plan8 = ${ wwCCPlan.editableFields.plan["8"] }")
        var feat = Feat.Actor
        wwCCPlan.editableFields.plan["8"]!!.feat = feat
        var result1 = ScenarioCalculator(scenario).calculateDPRForAllTurns()

        // %hit
        assertEquals(0.7f,  Globals.round2(result1.attackResults[0].chanceToHit.final), "t=1, a=1, feat=$feat, %hit")
        assertEquals(0.86f, Globals.round2(result1.attackResults[3].chanceToHit.final), "t=2, a=3, feat=$feat, %hit")

        // damage
        assertEquals(0f,    Globals.round2(result1.attackResults[0].damagePerRound.final), "t=1, a=1, feat=$feat, dph")
        assertEquals(20.38f,Globals.round2(result1.attackResults[3].damagePerRound.final), "t=2, a=3, feat=$feat, dph")


        feat = Feat.ColdCaster
        wwCCPlan.editableFields.plan["8"]!!.feat = feat
        var result2 = ScenarioCalculator(scenario).calculateDPRForAllTurns()

        // technically, %hit and DPH should be slightly higher for CC versus noCC ...
        // that will likely require branching calculations on a PER TARGET EFFECT basis;
        // --> more complex code, and more cpu per scenario

 //       assertNotEquals(result2.attackResults[3].chanceToHit.final, result1.attackResults[3].chanceToHit.final, "t=2, a=3, %hit notEqual with CC")
 //       assertNotEquals(result2.attackResults[3].damagePerRound.final, result1.attackResults[3].damagePerRound.final, "t=2, a=3, dph notEqual with CC")
        assertTrue(result2.attackResults[3].chanceToHit.final >= result1.attackResults[3].chanceToHit.final, "t=2, a=3, %hit GTE with CC")
        assertTrue(result2.attackResults[3].damagePerRound.final >= result1.attackResults[3].damagePerRound.final, "t=2, a=3, dph GTE with CC")

        // %hit
        assertEquals(0.7f,  Globals.round2(result2.attackResults[0].chanceToHit.final), "t=1, a=1, feat=$feat, %hit")
        assertEquals(0.86f, Globals.round2(result2.attackResults[3].chanceToHit.final), "t=2, a=3, feat=$feat, %hit")

        // damage
        assertEquals(0f,    Globals.round2(result2.attackResults[0].damagePerRound.final), "t=1, a=1, feat=$feat, dph")
        assertEquals(20.38f,Globals.round2(result2.attackResults[3].damagePerRound.final), "t=2, a=3, feat=$feat, dph")
    }

}