package com.vikinghelmet.dnd.dpr.scenario

import com.vikinghelmet.dnd.dpr.character.actions.ActionModifier
import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import com.vikinghelmet.dnd.dpr.spells.Spell
import com.vikinghelmet.dnd.dpr.spells.SpellsWithComplexRules
import com.vikinghelmet.dnd.dpr.spells.payload.Attack.Save
import com.vikinghelmet.dnd.dpr.util.AttackAdvantage
import com.vikinghelmet.dnd.dpr.util.Condition
import com.vikinghelmet.dnd.dpr.util.DiceBlock
import com.vikinghelmet.dnd.dpr.util.Globals
import kotlin.jvm.JvmName

interface TargetEffectCause {}

open class TargetEffect (
    val startTurn: Int,
    var cause: TargetEffectCause? = null,
    val probability: Float = 1f,

    val spellSaveDC: Int = 0,
    val save: Save? = null,

    @get:JvmName("getCustomFieldAttacksAgainstMe")
    var attacksAgainstMe:     AttackAdvantage = AttackAdvantage.normal,

    @get:JvmName("getCustomFieldAttacksAgainstOthers")
    var attacksAgainstOthers: AttackAdvantage = AttackAdvantage.normal,

    var disadvantageOnAttacksCaster: Boolean = false, // TODO: specify which caster ... CombatantWithStatus ?

    @get:JvmName("getCustomFieldAttackerAutoCritDamage")
    var attackerAutoCritDamage: Boolean = false, // when target is hit, double damage

    @get:JvmName("getCustomFieldAutoFailStrAndDexSaves")
    var autoFailStrAndDexSaves: Boolean = false, // Paralyzed, Petrified, Stunned,
    var unableToAct:            Boolean = false,

    // some effects are dependent on abilityType; these aren't modeled in Preconditions,
    // they can only be calculated at the moment a new spell is cast (old spell conditionally impacts new spell)

    @get:JvmName("getCustomFieldDisadvantageOnSave")
    var disadvantageOnSave:          AbilityType? = null, // always just 1 at a time
    var savePenaltyFilter:           AbilityType? = null, // never assigned
    var disadvantageOnAbilityChecks: AbilityType? = null, // either STR or ALL

    var attackerExtraDamageOnHit: DiceBlock = DiceBlock(),
    var savePenalty:   DiceBlock = DiceBlock(),     // this effect depends on savePenaltyFilter above
    var attackPenalty: DiceBlock = DiceBlock(),
    var damagePenalty: DiceBlock = DiceBlock(),
    var attackBonus:   DiceBlock = DiceBlock(),
    var saveBonus:     DiceBlock = DiceBlock(),

    var conditions:    MutableList<Condition> = mutableListOf()
    ) {

    init {
        if (cause is Spell) {
            val spell = cause as Spell
            conditions.addAll(spell.getSpellFailConditions())
            conditions.forEach { applyCondition(it) }
            applySpellName(spell.name)
        }

        if (cause == ActionModifier.RecklessAttack) {
            attacksAgainstOthers = AttackAdvantage.advantage
            attacksAgainstMe     = AttackAdvantage.advantage
        }
    }

    fun getAttacksAgainstMe() = attacksAgainstMe
    fun getAttacksAgainstOthers() = attacksAgainstOthers
    fun isAttackerAutoCritDamage() = attackerAutoCritDamage
    fun isAutoFailStrAndDexSaves() = autoFailStrAndDexSaves
    fun getDisadvantageOnSave() = disadvantageOnSave

    fun getSpell() = if (cause != null && cause is Spell) cause as Spell else null
    fun getDuration() = if (getSpell() == null) 1 else getSpell()?.getDuration() ?: 0

    fun hasSaveImpact() = savePenalty.isNotEmpty() || disadvantageOnSave != null || autoFailStrAndDexSaves

    fun isEmptyExceptForAdvantage(): Boolean {
        return  attacksAgainstOthers == AttackAdvantage.normal && !unableToAct && !attackerAutoCritDamage &&
                disadvantageOnAbilityChecks == null &&
                disadvantageOnSave == null &&
                !autoFailStrAndDexSaves &&
                savePenaltyFilter == null &&
                savePenalty.isEmpty() &&
                attackPenalty.isEmpty() &&
                damagePenalty.isEmpty() &&
                attackerExtraDamageOnHit.isEmpty()
    }

    fun isEmpty(): Boolean {
        return attacksAgainstMe == AttackAdvantage.normal && isEmptyExceptForAdvantage()
    }

    fun applyCondition(cond: Condition) {
        when (cond) {
            Condition.Blinded -> // Attack rolls against you have Advantage, and your attack rolls have Disadvantage.
            {
                attacksAgainstMe     = AttackAdvantage.advantage
                attacksAgainstOthers = AttackAdvantage.disadvantage
            }

            Condition.Frightened ->     // you have Disadvantage on ability checks and attack rolls while the source of fear is within line of sight.
            {
                disadvantageOnAbilityChecks = AbilityType.ALL
                attacksAgainstOthers = AttackAdvantage.disadvantage
            }

            Condition.Grappled ->       // speed=0; You have Disadvantage on attack rolls against any target other than the grappler.
            {
                attacksAgainstOthers = AttackAdvantage.disadvantage
            }

            Condition.Incapacitated ->  // you can’t take any action, Bonus Action, or Reaction; conc is broken; can't speak; disadvantage on initiative
            {
                unableToAct = true;
                // TODO: conc is broken; can't speak; disadvantage on initiative
            }

            Condition.Paralyzed ->      // Incapacitated++ ; speed=0; auto-fail STR and DEX saving throws; Attack rolls against you have Advantage; auto-crit (5 ft)
            {
                applyCondition(Condition.Incapacitated)
                autoFailStrAndDexSaves = true
                attacksAgainstMe = AttackAdvantage.advantage
                attackerAutoCritDamage = true // TODO: 5 feet
            }

            Condition.Petrified ->      // Incapacitated++ ; speed=0; auto-fail STR and DEX saving throws; Attack rolls against you have Advantage; resist damage; immune to poison
            {
                applyCondition(Condition.Incapacitated)
                autoFailStrAndDexSaves = true
                attacksAgainstMe = AttackAdvantage.advantage
                // TODO: resist damage; immune to poison
            }

            Condition.Poisoned ->       // You have Disadvantage on attack rolls and ability checks.
            {
                attacksAgainstOthers = AttackAdvantage.disadvantage
                disadvantageOnAbilityChecks = AbilityType.ALL
            }

            Condition.Prone ->          // half-speed to end; You have Disadvantage on attacks. Attacks against you: Advantage w/in 5 feet, else Disadvantage.
            {
                attacksAgainstOthers = AttackAdvantage.disadvantage
                // TODO: Attacks against you: Advantage w/in 5 feet, else Disadvantage.
            }
            Condition.Restrained ->    // speed=0; Attack rolls against you have Advantage, and your attack rolls have Disadvantage. You have Disadvantage on Dexterity saving throws
            {
                attacksAgainstMe     = AttackAdvantage.advantage
                attacksAgainstOthers = AttackAdvantage.disadvantage
                disadvantageOnSave   = AbilityType.Dexterity
            }
            Condition.Stunned ->        // Incapacitated++ ; auto-fail STR and DEX saving throws; Attack rolls against you have Advantage
            {
                applyCondition(Condition.Incapacitated)
                autoFailStrAndDexSaves = true

            }
            Condition.Unconscious ->    // Incapacitated+Prone++ ; speed=0; Attack rolls against you have Advantage; auto-fail STR and DEX saving throws;  auto-crit (5 ft)
            {
                attacksAgainstMe = AttackAdvantage.advantage
                applyCondition(Condition.Incapacitated)
                applyCondition(Condition.Prone)
                autoFailStrAndDexSaves = true
                attackerAutoCritDamage = true // TODO: within 5 feet
            }
            else -> {
//                logger.warn { "condition does not currently impact target effect: "+cond }
            }
        }
    }

    fun applySpellName(name: String)
    {
        // unless otherwise specified ...
        // - these one-off effects last for the spell duration, typically 1 min (also ends on a successful save)
        // - these spells require concentration for the duration, and spell ends if concentration is broken

        when (SpellsWithComplexRules.fromName (name)) {
            SpellsWithComplexRules.Bane -> {
                // target must subtract 1d4 from the attack roll or save
                attackPenalty = DiceBlock ("1d4")
                savePenalty = DiceBlock ("1d4")
            }
            SpellsWithComplexRules.Bless -> {
                // target may add 1d4 to the attack roll or save
                attackBonus = DiceBlock ("1d4")
                saveBonus = DiceBlock ("1d4")
            }
            SpellsWithComplexRules.BestowCurse -> {
                // Choose one ability. The target has Disadvantage on ability checks and saving throws made with that ability.\n-The target has Disadvantage on attack rolls against you.
                // TODO: user-selection ?
                disadvantageOnAttacksCaster = true
            }
            SpellsWithComplexRules.Blur -> {
                // Disadvantage on attack rolls against you
                disadvantageOnAttacksCaster = true
            }
            SpellsWithComplexRules.EnlargeReduce -> {
                // TODO: user-selection (Reduce) ?
                // Disadvantage on Strength checks and Strength saving throws
                disadvantageOnAbilityChecks = AbilityType.Strength
                disadvantageOnSave = AbilityType.Strength
            }
            SpellsWithComplexRules.Enthrall -> {
                // TODO: −10 penalty to Wisdom (Perception) checks and Passive Perception
            }
            SpellsWithComplexRules.FaerieFire -> {
                attacksAgainstMe = AttackAdvantage.advantage // TODO: only if attacker can see the target ?
            }
            SpellsWithComplexRules.GuidingBolt -> {
                // duration = 1 turn
                // does NOT require concentration
                // next attack roll made against it before the end of your next turn has Advantage
                attacksAgainstMe = AttackAdvantage.advantage
            }
            SpellsWithComplexRules.Hex -> {
                // duration = 1 hour
                // choose one ability when you cast the spell. The target has Disadvantage on ability checks made with the chosen ability
                // TODO: user-selection ?
            }
            SpellsWithComplexRules.HuntersMark -> { // 2024 rules // not sure if this belongs here or somewhere else
                // duration = 1 hour
                attackerExtraDamageOnHit = DiceBlock("1d6")
            }
            SpellsWithComplexRules.MindSliver -> {
                // subtract 1d4 from the next saving throw it makes before the end of your next turn
                // duration = 1 turn
                savePenalty = DiceBlock ("1d4")
            }
            SpellsWithComplexRules.OttosIrresistibleDance -> {
                // Disadvantage on Dexterity saving throws and attack rolls, and other creatures have Advantage on attack rolls against it
                disadvantageOnSave = AbilityType.Dexterity
                attacksAgainstOthers = AttackAdvantage.disadvantage
                attacksAgainstMe = AttackAdvantage.advantage
            }
            SpellsWithComplexRules.PhantasmalKiller -> {
                applyCondition(Condition.Frightened)
            }
            SpellsWithComplexRules.RayOfEnfeeblement -> {
                // On a failed save, the target has Disadvantage on Strength-based D20 Tests for the duration.
                // During that time, it also subtracts 1d8 from all its damage rolls.
                disadvantageOnAbilityChecks = AbilityType.Strength
                disadvantageOnSave = AbilityType.Strength
                damagePenalty = DiceBlock("1d8")
            }
            SpellsWithComplexRules.ShiningSmite -> {
                // attack rolls against it have Advantage
                attacksAgainstMe = AttackAdvantage.advantage
            }
            SpellsWithComplexRules.ViciousMockery -> {
                // spell duration is "instantaneous", but effect duration is 1 turn
                // Disadvantage on the next attack roll it makes before the end of its next turn
                attacksAgainstOthers = AttackAdvantage.disadvantage
            }
            else -> {}
        }
    }
    override fun toString(): String {
        val buf = StringBuilder()
        if (attacksAgainstMe     != AttackAdvantage.normal) buf.append("attacksAgainstMe=$attacksAgainstMe").append(";")
        if (attacksAgainstOthers != AttackAdvantage.normal) buf.append("attacksAgainstOthers=$attacksAgainstOthers").append(";")

        if (attackerAutoCritDamage) buf.append("attackerAutoCrit").append(";")
        if (!attackerExtraDamageOnHit.isEmpty()) buf.append("extraDamageOnHit="+attackerExtraDamageOnHit).append(";")

        // some effects are dependent on abilityType; these aren't modelled in Preconditions,
        // they can only be calculated at the moment a new spell is cast (old spell conditionally impacts new spell)
        if (disadvantageOnSave != null) buf.append("disadvantageOnSave="+disadvantageOnSave).append(";")
        if (autoFailStrAndDexSaves) buf.append("autoFailStrAndDexSaves="+autoFailStrAndDexSaves).append(";")
        if (savePenaltyFilter  != null) buf.append("savePenaltyFilter="+savePenaltyFilter).append(";")
        if (savePenalty.isNotEmpty()) buf.append("savePenalty="+savePenalty).append(";")

        if (unableToAct) buf.append("noActionOrBA").append(";")
        if (disadvantageOnAbilityChecks != null) buf.append("disadvantageOnAbilityChecks="+disadvantageOnAbilityChecks).append(";")
        if (attackPenalty.isNotEmpty()) buf.append("attackPenalty="+attackPenalty).append(";")
        if (damagePenalty.isNotEmpty()) buf.append("damagePenalty="+damagePenalty).append(";")

        return if (buf.length == 0) "" else "${Globals.getPercent(probability*100)}% = $buf"
    }
}