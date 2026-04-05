package com.vikinghelmet.dnd.dpr.util

import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import com.vikinghelmet.dnd.dpr.spells.SpellsWithComplexRules

data class TargetEffect (

    var attackerHasAdvantage: Boolean? = false,
    var attackerAutoCrit: Boolean? = false, // when target is hit, does attack automatically crit (double damage) ? // TODO ...
    var attackerExtraDamageOnHit: MutableList<String> = mutableListOf(), // 1d4, 1d6, ...

    // some effects are dependent on abilityType; these aren't modelled in Preconditions,
    // they can only be calculated at the moment a new spell is cast (old spell conditionally impacts new spell)
    var disadvantageOnSave: MutableList<AbilityType> = mutableListOf(),
    var autoFailSave: MutableList<AbilityType> = mutableListOf(),
    var savePenaltyFilter: MutableList<AbilityType> = mutableListOf(),
    var savePenalty: MutableList<String> = mutableListOf(),     //  1d4, 1d6, ... this effect depends on savePenaltyFilter above

    // remaining effects only matter when monsters fight back ...
    var disadvantageOnAttacks: Boolean? = false,
    var noActionOrBA: Boolean? = false,
    var disadvantageOnAbilityChecks: MutableList<AbilityType> = mutableListOf(), // this only matters when monsters fight back
    var attackPenalty: MutableList<String> = mutableListOf(),
    var damagePenalty: MutableList<String> = mutableListOf(),

    ) {
    fun hasSaveImpact() = savePenalty.isNotEmpty() || disadvantageOnSave.isNotEmpty() || autoFailSave.isNotEmpty()

    fun isEmpty(): Boolean {
        return !attackerHasAdvantage!! && !disadvantageOnAttacks!! && !noActionOrBA!! && !attackerAutoCrit!! &&
                disadvantageOnAbilityChecks.isEmpty() &&
                disadvantageOnSave.isEmpty() &&
                autoFailSave.isEmpty() &&
                savePenaltyFilter.isEmpty() &&
                savePenalty.isEmpty() &&
                attackPenalty.isEmpty() &&
                damagePenalty.isEmpty() &&
                attackerExtraDamageOnHit.isEmpty()
    }
    fun applyCondition(cond: Condition) {
        when (cond) {
            Condition.Blinded -> // Attack rolls against you have Advantage, and your attack rolls have Disadvantage.
            {
                attackerHasAdvantage = true
                disadvantageOnAttacks = true
            }

            Condition.Frightened ->     // you have Disadvantage on ability checks and attack rolls while the source of fear is within line of sight.
            {
                disadvantageOnAbilityChecks.add(AbilityType.ALL)
                disadvantageOnAttacks = true
            }

            Condition.Grappled ->       // speed=0; You have Disadvantage on attack rolls against any target other than the grappler.
            {
                disadvantageOnAttacks = true
            }

            Condition.Incapacitated ->  // you can’t take any action, Bonus Action, or Reaction; conc is broken; can't speak; disadvantage on initiative
            {
                noActionOrBA = true;
                // TODO: conc is broken; can't speak; disadvantage on initiative
            }

            Condition.Paralyzed ->      // Incapacitated++ ; speed=0; auto-fail STR and DEX saving throws; Attack rolls against you have Advantage; auto-crit (5 ft)
            {
                applyCondition(Condition.Incapacitated)
                autoFailSave.addAll (listOf(AbilityType.Strength, AbilityType.Dexterity))
                attackerHasAdvantage = true
                attackerAutoCrit = true // TODO: 5 feet
            }

            Condition.Petrified ->      // Incapacitated++ ; speed=0; auto-fail STR and DEX saving throws; Attack rolls against you have Advantage; resist damage; immune to poison
            {
                applyCondition(Condition.Incapacitated)
                autoFailSave.addAll (listOf(AbilityType.Strength, AbilityType.Dexterity))
                attackerHasAdvantage = true
                // TODO: resist damage; immune to poison
            }

            Condition.Poisoned ->       // You have Disadvantage on attack rolls and ability checks.
            {
                disadvantageOnAttacks = true
                disadvantageOnAbilityChecks.add(AbilityType.ALL)
            }

            Condition.Prone ->          // half-speed to end; You have Disadvantage on attacks. Attacks against you: Advantage w/in 5 feet, else Disadvantage.
            {
                disadvantageOnAttacks = true
                // TODO: Attacks against you: Advantage w/in 5 feet, else Disadvantage.
            }
            Condition.Restrained ->    // speed=0; Attack rolls against you have Advantage, and your attack rolls have Disadvantage. You have Disadvantage on Dexterity saving throws
            {
                attackerHasAdvantage = true
                disadvantageOnAttacks = true

                disadvantageOnSave.add(AbilityType.Dexterity)
            }
            Condition.Stunned ->        // Incapacitated++ ; auto-fail STR and DEX saving throws; Attack rolls against you have Advantage
            {
                applyCondition(Condition.Incapacitated)
                autoFailSave.addAll (listOf(AbilityType.Strength, AbilityType.Dexterity))

            }
            Condition.Unconscious ->    // Incapacitated+Prone++ ; speed=0; Attack rolls against you have Advantage; auto-fail STR and DEX saving throws;  auto-crit (5 ft)
            {
                applyCondition(Condition.Incapacitated)
                applyCondition(Condition.Prone)
                autoFailSave.addAll (listOf(AbilityType.Strength, AbilityType.Dexterity))
                attackerAutoCrit = true // TODO: within 5 feet
            }
            else -> {
//                println("# condition does not currently impact target effect: "+cond)
            }
        }
    }

    fun applySpellName(name: String)
    {
        // unless otherwise specified ...
        // - these one-off effects last for the spell duration, typically 1 min (also ends on a successful save)
        // - these spells require concentration for the duration, and spell ends if concentration is broken

        when (SpellsWithComplexRules.fromNameWithWS (name)) {
            SpellsWithComplexRules.Bane -> {
                // target must subtract 1d4 from the attack roll or save
                attackPenalty.add ("1d4")
                savePenalty.add ("1d4")
            }
            SpellsWithComplexRules.BestowCurse -> {
                // Choose one ability. The target has Disadvantage on ability checks and saving throws made with that ability.\n-The target has Disadvantage on attack rolls against you.
                // TODO: user-selection ?
                disadvantageOnAttacks = true
            }
            SpellsWithComplexRules.Blur -> {
                // Disadvantage on attack rolls against you
                disadvantageOnAttacks = true
            }
            SpellsWithComplexRules.EnlargeReduce -> {
                // TODO: user-selection (Reduce) ?
                // Disadvantage on Strength checks and Strength saving throws
                disadvantageOnAbilityChecks.add(AbilityType.Strength)
                disadvantageOnSave.add(AbilityType.Strength)
            }
            SpellsWithComplexRules.Enthrall -> {
                // TODO: −10 penalty to Wisdom (Perception) checks and Passive Perception
            }
            SpellsWithComplexRules.FaerieFire -> {
                attackerHasAdvantage = true // TODO: only if attacker can see the target ?
            }
            SpellsWithComplexRules.GuidingBolt -> {
                // duration = 1 turn
                // does NOT require concentration
                // next attack roll made against it before the end of your next turn has Advantage
                attackerHasAdvantage = true
            }
            SpellsWithComplexRules.Hex -> {
                // duration = 1 hour
                // choose one ability when you cast the spell. The target has Disadvantage on ability checks made with the chosen ability
                // TODO: user-selection ?
            }
            SpellsWithComplexRules.HuntersMark -> { // 2024 rules // not sure if this belongs here or somewhere else
                // duration = 1 hour
                attackerExtraDamageOnHit.add("1d6")
            }
            SpellsWithComplexRules.MindSliver -> {
                // subtract 1d4 from the next saving throw it makes before the end of your next turn
                // duration = 1 turn
                savePenalty.add ("1d4")
            }
            SpellsWithComplexRules.OttosIrresistibleDance -> {
                // Disadvantage on Dexterity saving throws and attack rolls, and other creatures have Advantage on attack rolls against it
                disadvantageOnSave.add(AbilityType.Dexterity)
                disadvantageOnAttacks = true
                attackerHasAdvantage = true
            }
            SpellsWithComplexRules.PhantasmalKiller -> {
                // Disadvantage on ability checks and attack rolls
                disadvantageOnAbilityChecks.add(AbilityType.ALL)
                disadvantageOnAttacks = true
            }
            SpellsWithComplexRules.RayOfEnfeeblement -> {
                // On a failed save, the target has Disadvantage on Strength-based D20 Tests for the duration.
                // During that time, it also subtracts 1d8 from all its damage rolls.
                disadvantageOnAbilityChecks.add(AbilityType.Strength)
                disadvantageOnSave.add(AbilityType.Strength)
                damagePenalty.add("1d8")
            }
            SpellsWithComplexRules.ShiningSmite -> {
                // attack rolls against it have Advantage
                attackerHasAdvantage = true
            }
            SpellsWithComplexRules.ViciousMockery -> {
                // spell duration is "instantaneous", but effect duration is 1 turn
                // Disadvantage on the next attack roll it makes before the end of its next turn
                disadvantageOnAttacks = true
            }
            else -> {}
        }
    }

    override fun toString(): String {
        val buf = StringBuilder()
        if (attackerHasAdvantage!!) buf.append("attackerHasAdvantage").append(";")
        if (attackerAutoCrit!!) buf.append("attackerAutoCrit").append(";")
        if (attackerExtraDamageOnHit.isNotEmpty()) buf.append("extraDamageOnHit="+attackerExtraDamageOnHit).append(";")

        // some effects are dependent on abilityType; these aren't modelled in Preconditions,
        // they can only be calculated at the moment a new spell is cast (old spell conditionally impacts new spell)
        if (disadvantageOnSave.isNotEmpty()) buf.append("disadvantageOnSave="+disadvantageOnSave).append(";")
        if (autoFailSave.isNotEmpty()) buf.append("autoFailSave="+autoFailSave).append(";")
        if (savePenaltyFilter.isNotEmpty()) buf.append("savePenaltyFilter="+savePenaltyFilter).append(";")
        if (savePenalty.isNotEmpty()) buf.append("savePenalty="+savePenalty).append(";")

        // remaining effects only matter when monsters fight back ...
        if (disadvantageOnAttacks!!) buf.append("disadvantageOnAttacks").append(";")
        if (noActionOrBA!!) buf.append("noActionOrBA").append(";")
        if (disadvantageOnAbilityChecks.isNotEmpty()) buf.append("disadvantageOnAbilityChecks="+disadvantageOnAbilityChecks).append(";")
        if (attackPenalty.isNotEmpty()) buf.append("attackPenalty="+attackPenalty).append(";")
        if (damagePenalty.isNotEmpty()) buf.append("damagePenalty="+damagePenalty).append(";")
        return buf.toString()
    }
}