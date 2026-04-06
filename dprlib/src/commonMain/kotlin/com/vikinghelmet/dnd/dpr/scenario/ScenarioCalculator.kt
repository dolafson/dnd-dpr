package com.vikinghelmet.dnd.dpr.scenario

import com.vikinghelmet.dnd.dpr.character.Character
import com.vikinghelmet.dnd.dpr.character.actions.ActionModifier
import com.vikinghelmet.dnd.dpr.character.feats.Feat
import com.vikinghelmet.dnd.dpr.character.inventory.MasteryProperty
import com.vikinghelmet.dnd.dpr.character.inventory.Weapon
import com.vikinghelmet.dnd.dpr.spells.Spell
import com.vikinghelmet.dnd.dpr.spells.SpellAttack
import com.vikinghelmet.dnd.dpr.turn.ActionCalculator
import com.vikinghelmet.dnd.dpr.turn.Attack
import com.vikinghelmet.dnd.dpr.turn.AttackResult
import com.vikinghelmet.dnd.dpr.util.Globals
import com.vikinghelmet.dnd.dpr.util.TargetEffect

class ScenarioCalculator(
    val scenario: Scenario,    
    val effectManager: EffectManager = EffectManager(ArrayList())
)
{
    val actionCalculator = ActionCalculator(scenario, effectManager)

    fun calculateDPRForAllTurns(): ScenarioResult {
        var turnId = 1
        var scenarioTotalDamage = 0f
        val attackResults = ArrayList<AttackResult>()

        for (turn in scenario.turns) {
            var dpr = 0f
            var actionCount = 1

            for (attack in turn.attacks) {
                val resultsForAttack = calculateDPR(turnId, actionCount, attack)

                if (effectManager.hasChanceOfFailure()) {
                    effectManager.assumeSpellEffectSuccess = false
                    val resultsForAttackWithNoSpellPreconditions = calculateDPR(turnId, actionCount, attack)
                    effectManager.assumeSpellEffectSuccess = true
                    // TODO: find a way to merge the two sets of results ...
                }
                
                for (result in resultsForAttack) {
                    dpr += result.damagePerRound.select (result.avgMinMaxSelection)
                    scenarioTotalDamage += result.damageFullEffect.select (result.avgMinMaxSelection)
                }
                actionCount++

                attackResults.addAll(resultsForAttack)
            }

            effectManager.pruneEffectsAtEndOfTurn(turnId)
            turnId++
        }

        return ScenarioResult(scenario, attackResults, scenarioTotalDamage)
    }

    private fun calculateDPR(turnId: Int, actionId: Int, attack: Attack): List<AttackResult> {
        val spell = if (attack.action is Spell) attack.action else null

        return if (attack.action is Weapon) {
            getWeaponAttackDpr(turnId, actionId, attack)
        }
        else if (spell != null) {
            calculateSpellDPR(turnId, actionId, attack, spell)
        }
        else {
            emptyList()
        }
    }

    private fun getWeaponAttackDpr(turnId: Int, actionId: Int, attack: Attack): MutableList<AttackResult> {
        val resultList = mutableListOf<AttackResult>()
        val weapon = attack.action as Weapon
        var effect = 1

        resultList.add(actionCalculator.getMeleeOrRangeDPR(weapon, attack, turnId, actionId, effect++))

        // some classes gain extra attacks at level 5; fighters get even more extra attacks later
        repeat(scenario.character.getExtraAttacks()) {
            val extraAttack = attack.copy(actionModifiers = mutableListOf(ActionModifier.ExtraAttack))
            resultList.add(actionCalculator.getMeleeOrRangeDPR(weapon, extraAttack, turnId, actionId, effect++))
        }

        if (weapon.hasMasteryProperty(MasteryProperty.Cleave) && scenario.numTargets > 1 && scenario.targetSpacing <= 5) {
            val weaponWithNoBonusDamage = object : Weapon(weapon.name, weapon.damage) {
                override fun getBonusDamage(character: Character, isBonusAction: Boolean) = 0
            }

            val secondAttack = Attack(attack.monster, weaponWithNoBonusDamage, mutableListOf(ActionModifier.Cleave))
            resultList.add(actionCalculator.getMeleeOrRangeDPR(weaponWithNoBonusDamage, secondAttack, turnId, actionId, effect++))
        }

        if (scenario.character.isFeatEnabled(Feat.ColdCaster.getNameWithWS())) {
            // http://dnd2024.wikidot.com/feat:cold-caster ...
            // Frostbite. Once per turn when you hit a creature with an attack roll and deal Cold damage,
            // you can temporarily negate the creature’s defenses. The creature subtracts 1d4 from the next
            // saving throw it makes before the end of your next turn.
            // TODO: should also check if damage type = Cold (though WW always adds cold damage to weapons, once/round)
            val probability = resultList.first().chanceToHit.avg
            effectManager.add(TargetEffect(turnId, Feat.ColdCaster, probability, savePenalty = mutableListOf("1d4")))
            Globals.debug("after adding CC feat, effects = " + effectManager)
        }

        effectManager.pruneEffectsWaitingForNextAttack(null)
        return resultList
    }

    private fun calculateSpellDPR(turnId: Int, actionId: Int, attack: Attack, spell: Spell): List<AttackResult>
    {
        val resultList = mutableListOf<AttackResult>()
        var effectCount = 1
        Globals.debug("spell = ${spell.fullString()}")

        for (spellAttack in spell.getSpellAttacks()) {
            Globals.debug("spell = ${spell.name}, spellAttack = $spellAttack")

            // if there is nothing special going on, simply process the spell and collect its results
            if (effectManager.runningEffectList.isEmpty() || spellAttack.getNumTargetsAffected(scenario) <= 1) {
                resultList.add (actionCalculator.getSpellDPR(spellAttack, spell, attack, turnId, actionId, effectCount++))
                continue
            }

            // this weird bit of logic is needed for AreaOfEffect spells, which hit multiple targets
            // one of the targets may have a save penalty, while others may not
            // to handle that, break them apart into separate spellAttacks, with varying conditions ...
            val copyMinusOne = SpellAttack(spellAttack, scenario)
            resultList.add (actionCalculator.getSpellDPR(spellAttack, spell, attack, turnId, actionId, effectCount++))
            resultList.add (actionCalculator.getSpellDPR(copyMinusOne, spell, attack, turnId, actionId, effectCount++))
        }

        effectManager.add(TargetEffect(turnId, spell, resultList.first().chanceToHit.avg))
        return resultList
    }
}
