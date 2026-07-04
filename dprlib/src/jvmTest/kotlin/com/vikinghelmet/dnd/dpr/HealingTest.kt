package com.vikinghelmet.dnd.dpr

import com.vikinghelmet.dnd.dpr.action.Attack
import com.vikinghelmet.dnd.dpr.action.Turn
import com.vikinghelmet.dnd.dpr.action.enums.DamageType
import com.vikinghelmet.dnd.dpr.scenario.combat.ActionGoal
import com.vikinghelmet.dnd.dpr.scenario.combat.Combat
import com.vikinghelmet.dnd.dpr.scenario.combat.HealingAction
import com.vikinghelmet.dnd.dpr.scenario.combat.TurnOptionRanking
import com.vikinghelmet.dnd.dpr.scenario.combat.location.Location
import com.vikinghelmet.dnd.dpr.scenario.combat.results.DamageResult
import com.vikinghelmet.dnd.dpr.spells.Spell
import com.vikinghelmet.dnd.dpr.spells.SpellsWithComplexRules
import com.vikinghelmet.dnd.dpr.util.Constants
import com.vikinghelmet.dnd.dpr.util.Globals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class HealingTest {

    @Test
    fun getClericHealing() {
        TestUtil.dependency()
        val combat = Combat(0,listOf(TestUtil.kael), listOf(Globals.getMonster("Goblin")))

        // force melee range
        combat.teamA[0].location = Location(0, 0)
        combat.teamB[0].location = Location(1, 0)

        val kael = combat.teamA[0]
        kael.currentHP = 3 // force lowHP

        println("before turn, kael.currentHp = ${kael.currentHP}")

        while (kael.currentHP < kael.getHP()) {
            HealingAction(combat, kael).takeAction()
            assertTrue(kael.currentHP > 3)
        }

        assertEquals(kael.currentHP, kael.getHP())
    }

    @Test
    fun getClericPreferredTurnWhenDamaged() {
        TestUtil.dependency()
        val combat = Combat(0,listOf(TestUtil.kael), listOf(Globals.getMonster("Goblin")))

        // force melee range
        combat.teamA[0].location = combat.teamB[0].location.copy()

        combat.teamA[0].currentHP = 3

        println("getActionsAvailable = ${ combat.teamA[0].getActionsAvailable()}")

        for (range in listOf(5,60)) {
            var preferred = combat.teamA[0].getPreferredTurn(ActionGoal.Heal, combat.teamB[0], range)
            val spellName = preferred!!.first.attacks.map { it.action.getActionName() }.toList()[0]
            when (range) {
                5 -> {
                    //assertEquals(TurnOptionRanking.SpellWithDeathPrevention, preferred.second)
                    //assertEquals("Spare the Dying", spellName)
                    assertEquals(SpellsWithComplexRules.ChannelDivinityPreserveLife, SpellsWithComplexRules.fromName(spellName))
                    assertEquals(TurnOptionRanking.SpellWithRestoreHPAOE, preferred.second)
                }
                60 -> {
                    assertEquals(TurnOptionRanking.SpellWithRestoreHP, preferred.second)
                    assertEquals("Healing Word", spellName)
                }
            }
        }
    }

    @Test
    fun prayerOfHealing() {
        TestUtil.dependency()
        val combat = Combat(0,listOf(TestUtil.kael3), listOf(Globals.getMonster("Goblin")))

        // TODO - there are multiple things broken with this spell
        // 1. it routinely returns 0 healing (probably due to #2 below ...)
        // 2. it shows up with 2024 data (while kael is 2014)
        // 3. due to a long casting time (10 mins), it should ONLY be used AFTER combat

        // force melee range
        combat.teamA[0].location = combat.teamB[0].location.copy()

        val kael = combat.teamA[0]
        val goblin = combat.teamB[0]
        kael.currentHP = 3

        val available = kael.getActionsAvailable()
        println("getActionsAvailable = ${ available}")

        val meleeRangeList = available.mapOfLists[Constants.MELEE_RANGE]!!

        meleeRangeList.filter { it is Spell }.forEach { it as Spell
            println("is2014=${it.is2014()}, $it")
        }

        val prayer = meleeRangeList.first { it.getActionName() == "Prayer of Healing" } as Spell
        assertNotNull(prayer)

        println("casting time = ${prayer.getCastingTime()}")

        assertFalse (kael.isSpellViable (goblin, combat, Turn(listOf(Attack(goblin, prayer)))))
    }

    @Test
    fun spareTheDying() {
        val input = "Spare the Dying"
        val words = input.split(Regex("[\\s_]+"))
        val joined = words.joinToString("") { word -> word.lowercase().replaceFirstChar { it.uppercase() } }
        assertEquals("SpareTheDying", joined)
    }

    @Test
    fun cureWounds() {
        TestUtil.dependency()
        val combat = Combat(0,listOf(TestUtil.kael, TestUtil.leif), listOf(Globals.getMonster("Goblin")))

        // force melee range
        combat.teamA[0].location = Location(-1, 0)
        combat.teamA[1].location = Location(0, 0)
        combat.teamB[0].location = Location(1, 0)

        val kael = combat.teamA[0]
        val leif = combat.teamA[1]

        kael.applyDamage(0, listOf(DamageResult(kael.getHP()+1, DamageType.fire)))

        val goal = leif.getActionGoal(combat)
        assertEquals(ActionGoal.Heal, goal)

        val turn = leif.getPreferredTurn(goal, kael, Constants.MELEE_RANGE, combat)
        println("turn = $turn")

        val spell =  turn!!.first.getSpell()!!
        assertEquals("Cure Wounds", spell.name)

        val healAmount = leif.getHealingAmount(spell, true)
        println("healAmount = $healAmount")
        assertTrue(healAmount > 0)
    }
}