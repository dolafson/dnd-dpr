package com.vikinghelmet.dnd.dpr.turn
import com.vikinghelmet.dnd.dpr.character.Character
import com.vikinghelmet.dnd.dpr.monsters.Monster
import com.vikinghelmet.dnd.dpr.scenario.EffectManager
import com.vikinghelmet.dnd.dpr.spells.SaveResult
import com.vikinghelmet.dnd.dpr.spells.Spell
import com.vikinghelmet.dnd.dpr.spells.SpellAttack
import com.vikinghelmet.dnd.dpr.util.DiceBlock
import com.vikinghelmet.dnd.dpr.util.DiceBlockHelper
import com.vikinghelmet.dnd.dpr.util.Globals
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

enum class AvgMinMaxSelection { avg, min, max }

data class AvgMinMax(var avg: Float, var min: Float, var max: Float) {
    fun select(choice: AvgMinMaxSelection): Float {
        return when(choice) {
            AvgMinMaxSelection.avg -> avg
            AvgMinMaxSelection.min -> min
            else -> max
        }
    }

    fun half(diceBlock: DiceBlock): AvgMinMax {
        val halfAvg = if (diceBlock.isEmpty()) avg/2 else avg/2 - 0.25f
        return AvgMinMax(halfAvg, (min.toInt() / 2).toFloat(), (max.toInt() / 2).toFloat())
    }
    fun debug(label: String) {
        if (Globals.debug) {
            System.err.println(String.format("%s, (avg, min, max) = (%2.2f, %2.2f, %2.2f)", label, avg, min, max))
        }
    }
}


//    https://docs.google.com/document/d/11eTMZPPxWXHY0rQEhK1msO-40BcCGrzArSl4GX4CiJE/edit?tab=t.0


class DamagePerRound(var character: Character)
{
    val effectSaveDC = character.getSpellSaveDC()
    val isLucky = character.isLucky()

    fun debug() { if (Globals.debug) System.err.println() }
    fun debug(str:String) { if (Globals.debug) System.err.println(str) }

    fun getAvgMinMax(diceBlock: DiceBlock, bonusDamage: Int): AvgMinMax {
        val avg = averageDamage(diceBlock, bonusDamage, character.isGreatWeaponFighting(), character.isElementalAdept())
        val min = diceBlock.min()+bonusDamage.toFloat()
        val max = diceBlock.max()+bonusDamage.toFloat()
        return AvgMinMax(avg, min, max)
    }

// Base probability of hitting a given AC with a given attack bonus.
    fun toHit(atk: Int, ac: Int, autohit: Int): Float {
        return max(min(21 - ac + atk, 19), 21 - autohit)  /  20f
    }

// Probability of a hit with the Halfling Luck feature:
// that is, the probability of a hit, or a 1 becoming a hit.
    fun luckP(p: Float, isLucky: Boolean): Float
    {
        return if (!isLucky) p else p * (1 + 1 / 20)
    }

// Transform a base hit probability to include disadvantage and (if applicable) Halfling Luck.
    fun disadvP(p: Float, isLucky: Boolean): Float
    {
        // Disadvantage: You have to hit with both dice.
        val d = p * p
        // if lucky you get a 1 on either die, you missed, but you get another shot.
        return if (!isLucky) d else d + 2 * p * p / 20
    }

// Transform a base hit probability to include advantage and (if applicable) Halfling Luck.
    fun advanP(p: Float, isLucky: Boolean): Float
    {
        val a = 2 * p - p * p
        return if (!isLucky) a else a + (2 / 20 * (1 - p) - 1 / 400) * p
    }

// Transform a base hit probability to include Elven Accuracy and (if applicable,
// though it's difficult to imagine why it would be) Halfling Luck.
    fun elvenP(p: Float, isLucky: Boolean): Float
    {
        val e = p * p * p + 3 * p - 3 * p * p
        return if (!isLucky) e else e + (3 * (1 - p) * (1 - p) / 20 - 3 * (1 - p) / 400 + 1 / 8000) * p
    }

// Utility fun: takes the probability distribution of a value X and computes
// the probability distribution of X + D (for a single die of size D).
    fun convolve(arr: MutableList<Float>, die: Int)
    {
        for (i in 1..die) {
            arr.add(0f)
        }

        for (j in arr.size-1 downTo 0) {
            arr[j] = 0f
            for (k in j-1 downTo (j - die)){
                if (k < 0) break
                arr[j] += arr[k]
            }
            arr[j] = arr[j] / die
        }
    }

// Compute the probability distribution of a pile of dice rolls
    fun buffDist(diceBlock: DiceBlock): List<Float>
    {
        val arr: MutableList<Float> = mutableListOf(1f)

        for (i in 0..<diceBlock.d4) {
            convolve(arr, 4)
        }
        for (i in 0..<diceBlock.d6) {
            convolve(arr, 6)
        }
        for (i in 0..<diceBlock.d8) {
            convolve(arr, 8)
        }
        for (i in 0..<diceBlock.d10) {
            convolve(arr, 10)
        }
        for (i in 0..<diceBlock.d12) {
            convolve(arr, 12)
        }
        return arr
    }

// Compute the attack probability taking all variables into account:
// attack bonus, AC, advantage, Elven Accuracy, and bonus dice (e.g. Bless).
    fun totalProb(atk: Int, ac: Int, isAdvan: String, isElven: Boolean, isLucky: Boolean, autohit: Int,
                  bonusDiceToSave: DiceBlock, penaltyDiceToSave: DiceBlock
): Float
    {
        val arr    = buffDist(bonusDiceToSave)
        val negArr = buffDist(penaltyDiceToSave)
        var blessed = 0f

        if (isAdvan == "No Advantage") {
            for (i in arr.indices) {
                for (j in negArr.indices) {
                    blessed += (arr[i] * negArr[j] * luckP(toHit(atk + i - j, ac, autohit), isLucky))
                }
            }
        }
        if (isAdvan == "Advantage" && isElven) {
            for (i in arr.indices) {
                for (j in negArr.indices) {
                    blessed += (arr[i] * negArr[j] * elvenP(toHit(atk + i - j, ac, autohit), isLucky))
                }
            }
        }
        if (isAdvan == "Advantage" && !isElven) {
            for (i in arr.indices) {
                for (j in negArr.indices) {
                    blessed += (arr[i] * negArr[j] * advanP(toHit(atk + i - j, ac, autohit), isLucky))
                }
            }
        }
        if (isAdvan == "Disadvantage") {
            for (i in arr.indices) {
                for (j in negArr.indices) {
                    blessed += (arr[i] * negArr[j] * disadvP(toHit(atk + i - j, ac, autohit), isLucky))
                }
            }
        }

        return blessed
    }

// Compute the crit chance given the relevant parameters:
// the crit threshold (should normally be 20, except for things
// like Champion features), and the presence of Advantage,
// Elven Accuracy, and Halfling Luck.
    fun critChance(threshold: Int, isAdvan: String, isElven: Boolean, isLucky: Boolean): Float
    {
        if (threshold > 20 || threshold < 1) {
            return 0f
        }
        val p = (21 - threshold) / 20f
        var c = 0f
        if (isAdvan == "No Advantage") {
            c = luckP(p, isLucky)
        }
        if (isAdvan == "Advantage" && !isElven) {
            c = advanP(p, isLucky)
        }
        if (isAdvan == "Advantage" && isElven) {
            c = elvenP(p, isLucky)
        }
        if (isAdvan == "Disadvantage") {
            c = disadvP(p, isLucky)
        }

        return c
    }

// **********************************************************************
// The following section focuses on computing SAVE probabilities,
// which have the notable difference that there are no crits.

// Compute the base save probability based on bonus and DC.
    fun toSave(bonus: Int, dc: Int): Float
    {
        return max(min(21 - dc + bonus, 20), 0) / 20f
    }

// Probability of a hit with the Halfling Luck feature:
// that is, the probability of a hit, or a 1 becoming a hit.
    fun luckS(p: Float, isLucky: Boolean): Float
    {
        return if (!isLucky) p else if (p == 1f) p else  p * (1 + 1 / 20f)
    }

// Transform a base hit probability to include disadvantage and (if applicable) Halfling Luck.
    fun disadvS(p: Float, isLucky: Boolean): Float
    {
        // Disadvantage: You have to hit with both dice.
        val d = p * p
        // Lucky: if you get a 1 on either die - and you missed - you get another shot.
        return if (!isLucky) d else if (p == 1f) p else d + 2 * p * p / 20f
    }

// Transform a base hit probability to include advantage and (if applicable) Halfling Luck.
    fun advanS(p: Float, isLucky: Boolean): Float
    {
        val a = 2 * p - p * p
        return if (!isLucky) a else if (p == 1f) p else a + (2 / 20f * (1 - p) - 1 / 400f) * p
    }


// Compute the save probability taking all variables into account:
// save bonus, DC, advantage, and bonus dice (e.g. Bless).
    fun saveProb(targetSaveBonus: Int, isAdvan: String, bonusDiceToSave: DiceBlock, penaltyDiceToSave: DiceBlock): Float
    {
        val dc: Int = effectSaveDC
        val arr    = buffDist(bonusDiceToSave)
        val negArr = buffDist(penaltyDiceToSave)
        var blessed = 0f

        if (isAdvan == "No Advantage") {
            for (i in arr.indices) {
                for (j in negArr.indices) {
                    blessed += (arr[i] * negArr[j] * luckS(toSave(targetSaveBonus + i - j, dc), isLucky))
                }
            }
        }
        if (isAdvan == "Advantage") {
            for (i in arr.indices) {
                for (j in negArr.indices) {
                    blessed += (arr[i] * negArr[j] * advanS(toSave(targetSaveBonus + i - j, dc), isLucky))
                }
            }
        }
        if (isAdvan == "Disadvantage") {
            for (i in arr.indices) {
                for (j in negArr.indices) {
                    blessed += (arr[i] * negArr[j] * disadvS(toSave(targetSaveBonus + i - j, dc), isLucky))
                }
            }
        }

        return blessed
    }

// Compute the average DPR of a single attack.
// Takes into account the chance of a hit, the damage from a hit,
// the chance of a crit, and the damage on a crit (INCLUDING the damage
// already dealt by an ordinary hit).
    fun attackDPR(hitChance: Float, hitDamage: Float, critChance: Float, critDamage: Float): Float
    {
        return (hitChance - critChance) * hitDamage + critChance * critDamage
    }

// Compute the average output of a series of dice with a bonus.
// The last two arguments are booleans to indicate whether the features
// Great Weapon Fighting (Fighting Style) and Elemental Adept (Feat) are active.
    fun averageDamage(diceBlock: DiceBlock, bonusDamage: Int, isGWF: Boolean, isEA: Boolean): Float
    {
        val avgRollPerDie: MutableList<Float> = if (isGWF) {
            if (isEA) {
                mutableListOf(3.125f, (38f / 9), (169f / 32), 6.32f, (529f / 72))
            } else {
                mutableListOf(3f, (25f / 6), 5.25f, 6.3f, (22f / 3))
            }
        } else {
            if (isEA) {
                mutableListOf(2.75f, (11f / 3), 4.625f, 5.6f, (79f / 12))
            } else {
                mutableListOf(2.5f, 3.5f, 4.5f, 5.5f, 6.5f)
            }
        }

        var sum: Float = bonusDamage + 0f

        for (i in avgRollPerDie.indices) {
            sum += avgRollPerDie[i] * diceBlock[i]
        }
        return sum
    }


    fun durationInRounds(saveEvery: Boolean, chanceToHit: Float, maxDuration: Int? = null): Float
    {
        // 28. Average Duration (In Rounds)
        //  =IF(AND($G$20="Save Every Round",Y6<1), IF($H$19,(1/(1-Y6)-1)*(1-Y6^$I$19),1/(1-Y6)-1),IF($H$19,$I$19*Y6,"INFINITE"))

        val hasMaxDuration: Boolean = (maxDuration != null)

        return if (saveEvery && chanceToHit < 1f) {
            if (hasMaxDuration) {
                (1/(1-chanceToHit)-1) * (1-chanceToHit.pow(maxDuration!!))
            } else {
                1/(1-chanceToHit)-1
            }
        } else {
            if (hasMaxDuration) {
                maxDuration!! * chanceToHit
            } else {
                0f // Instantaneous
                //10000000f // TODO: infinite duration ?
            }
        }
    }

    // ==========================================================
    // here is where the fun really begins

    fun getSpellDPR(spellAttack: SpellAttack, spell: Spell, attack: Attack, monster: Monster, character: Character, effectManager: EffectManager): AttackResult {
        return if (spellAttack.isNoDamageAttack()) {
            getNoDamageSpellDPR (spell)
        }
        else if (spellAttack.isSavingThrowAttack()) {
            getSavingThrowSpellDPR (spellAttack, spell, attack, monster)
        } else {
            getMeleeOrRangeDPR (spellAttack, attack, monster, effectManager)
        }
    }

    fun getNoDamageSpellDPR(spell: Spell): AttackResult {
        val duration = 1f * (spell.getDuration() ?: 0)
        return AttackResult(
            1,
            AvgMinMax(100f,100f,100f),
            AvgMinMax(0f,0f,0f),
            AvgMinMax(0f,0f,0f),
            AvgMinMax(duration, duration, duration),
            AvgMinMax(0f,0f,0f)
        )
    }

    fun getSavingThrowSpellDPR(spellAttack: SpellAttack, spell: Spell, attack: Attack, monster: Monster): AttackResult
    {
        debug("\n##### getSavingThrowSpellDPR: $spellAttack")
        val isTargetEvasive = monster.properties.isEvasive()
        val preconditions = attack.preconditions ?: Preconditions()
        val bonusDiceToSave = preconditions.bonusDiceToSave ?: DiceBlock(0, 0, 0, 0, 0)
        val penaltyDiceToSave = preconditions.penaltyDiceToSave ?: DiceBlock(0, 0, 0, 0, 0)

        // ----------------------------------------------------------------------------------------------------
        // If you get a bonus on only one hit or target, such as with the Evoker's "Empowered Evocation" ability, you can enter the bonus here.
        // we may be able to extract this from spell properties ?
        val bonusDamage = preconditions.bonusDamage ?: 0
        val bonusDamageOnFirstHit = preconditions.bonusDamageOnFirstHit ?: DiceBlock(0, 0, 0, 0, 0)

        // TODO: add support for Hunters Mark damage on melee/range spell attacks
        // (this can be done manually via bonusDamageOnFirstHit, but more explicit support would be good)

        val maxNumberOfTargets = if (spellAttack.isAreaOfEffectBig()) 3 else 1 // TODO: improve this

        val numberOfTargets = maxNumberOfTargets // Math.min(attack.numTargets ?: 1, maxNumberOfTargets)
        val saveResult = spellAttack.getSaveResult()
        debug()
        debug("spell duration (max): " + spell.getDuration())
        debug("spell damage:         " + spellAttack.getDamageDice())
        debug("num effects/targets:  $numberOfTargets")
        debug("spell penalty dice:   $penaltyDiceToSave")
        debug()

        var targetSaveBonus = 0
        val save = spellAttack.attackPayload.save

        val ability = save?.saveAbility
        if (ability != null) {
            debug("spell save ability      = $ability")
            targetSaveBonus = monster.properties.getMod(ability)
            debug("target save proficiency = $targetSaveBonus")
            debug("spell caster save DC    = "+character.getSpellSaveDC())
            debug("spell save result:      = $saveResult")
            debug()
        }

        val noSave      = saveResult == SaveResult.NOT_APPLICABLE // TODO: null ?
        val saveForHalf = saveResult == SaveResult.HALF_DAMAGE
        val saveEvery   = saveResult == SaveResult.SPELL_ENDS // TODO: also condition ends ???

        // 6. Chance to Hit (Hit%)
        // Use saveProb to calculate the save probability for the target (monster),
        // then invert that prob to get our chance of "hitting" with the spell.  When
        // the monster rolls with advantage - eg has "Magic Resistance" - this yields
        // the player's minimum chance to hit
        var chanceToHit = AvgMinMax(
            1 - saveProb(targetSaveBonus,"No Advantage", bonusDiceToSave, penaltyDiceToSave),
            1 - saveProb(targetSaveBonus,"Advantage", bonusDiceToSave, penaltyDiceToSave),
            1 - saveProb(targetSaveBonus,"Disadvantage", bonusDiceToSave, penaltyDiceToSave)
        )

        if (preconditions.autoFailSave == true) {
            debug("autoFailSave is enabled, forcing chanceToHit to 100")
            chanceToHit = AvgMinMax(100f, 100f, 100f)
        }

        chanceToHit.debug("Chance to Hit")

        if (spellAttack.getDamageDice().isEmpty()) {
            debug ("This spell never directly creates damage")
            return AttackResult(
                numberOfTargets, chanceToHit, AvgMinMax(0f,0f,0f),
                AvgMinMax(0f,0f,0f), AvgMinMax(0f,0f,0f), AvgMinMax(0f,0f,0f)
            )
        }

        val fullDamage: AvgMinMax = getAvgMinMax(spellAttack.getDamageDice(), bonusDamage)
        fullDamage.debug("Full Damage")

        val halfDamage = fullDamage.half(spellAttack.getDamageDice())
        halfDamage.debug("Half Damage")
        debug()

        val fullDamageFirstHit: AvgMinMax = getAvgMinMax(bonusDamageOnFirstHit, bonusDamage)
        fullDamageFirstHit.debug("Full Damage (First Hit)")

        val halfDamageFirstHit = fullDamageFirstHit.half(bonusDamageOnFirstHit)
        halfDamageFirstHit.debug("Half Damage (First Hit)")

        val chanceofAtLeastOneHit = AvgMinMax(
            1 - (1 - chanceToHit.avg).pow(numberOfTargets),
            1 - (1 - chanceToHit.min).pow(numberOfTargets),
            1 - (1 - chanceToHit.max).pow(numberOfTargets),
        )
        chanceofAtLeastOneHit.debug("Chance of at least one hit")
        debug()

        val damagePerTarget = AvgMinMax(
            chanceToHit.avg * fullDamage.avg + (1 - chanceToHit.avg) * halfDamage.avg,
            chanceToHit.min * fullDamage.avg + (1 - chanceToHit.min) * halfDamage.avg,
            chanceToHit.max * fullDamage.avg + (1 - chanceToHit.max) * halfDamage.avg,
        )
        damagePerTarget.debug("Damage Per Target")

        // 12. Damage per Failed Save       =IF(G15="Evasion",D228,D225)
        val damagePerFailedSave = AvgMinMax(
            if (isTargetEvasive) halfDamage.avg else fullDamage.avg,
            if (isTargetEvasive) halfDamage.min else fullDamage.min,
            if (isTargetEvasive) halfDamage.max else fullDamage.max
        )
        damagePerFailedSave.debug("Damage Per Failed Save")

        // 15. Damage per Successful Save   =IF(G15="No Save",D225,IF(G15="Save for Half",D228,0))
        val damagePerSuccessfulSave = AvgMinMax(
            if (noSave) fullDamage.avg else if (saveForHalf) halfDamage.avg else 0f,
            if (noSave) fullDamage.min else if (saveForHalf) halfDamage.min else 0f,
            if (noSave) fullDamage.max else if (saveForHalf) halfDamage.max else 0f,
        )
        damagePerSuccessfulSave.debug("Damage Per Successful Save")

        // 21. Damage per Hit               =Y6*Y12+(1-Y6)*Y15
        val damagePerHit = AvgMinMax(
            chanceToHit.avg * damagePerFailedSave.avg + (1 - chanceToHit.avg) * damagePerSuccessfulSave.avg,
            chanceToHit.min * damagePerFailedSave.avg + (1 - chanceToHit.min) * damagePerSuccessfulSave.avg,
            chanceToHit.max * damagePerFailedSave.avg + (1 - chanceToHit.max) * damagePerSuccessfulSave.avg,
        )
        damagePerHit.debug("Damage Per Hit")
        debug()

        // 9. Average DPR                  =L13*Y21+Y18*IF(G15="Evasion",D234,D231)
        val evasion = (if (isTargetEvasive) halfDamageFirstHit.avg else fullDamageFirstHit.avg)

        val averageDPR = AvgMinMax(
            numberOfTargets * damagePerHit.avg + chanceofAtLeastOneHit.avg * evasion,
            numberOfTargets * damagePerHit.min + chanceofAtLeastOneHit.min * evasion,
            numberOfTargets * damagePerHit.max + chanceofAtLeastOneHit.max * evasion,
        )
        averageDPR.debug("Average Damage Per Round")

        // 28. Average Duration (In Rounds)
        //  =IF(AND($G$20="Save Every Round",Y6<1), IF($H$19,(1/(1-Y6)-1)*(1-Y6^$I$19),1/(1-Y6)-1),IF($H$19,$I$19*Y6,"INFINITE"))

        val averageDuration = AvgMinMax(
            durationInRounds(saveEvery, chanceToHit.avg, spell.getDuration()),
            durationInRounds(saveEvery, chanceToHit.min, spell.getDuration()),
            durationInRounds(saveEvery, chanceToHit.max, spell.getDuration()),
        )

        averageDuration.debug("Average Duration (In Rounds)")

        // 31. Average Total Damage Over Time
        // if spell duration is "Instantaneous", damage over time = damage in first round
        val averageTotalDamageOverTime = if (averageDuration.max <= 0) averageDPR else
            AvgMinMax(
            averageDuration.avg * damagePerFailedSave.avg + damagePerSuccessfulSave.avg,
            averageDuration.min * damagePerFailedSave.avg + damagePerSuccessfulSave.avg,
            averageDuration.max * damagePerFailedSave.avg + damagePerSuccessfulSave.avg
        )

        averageTotalDamageOverTime.debug("Average Total Damage Over Time")

        debug(String.format("avg %%hit:         %2.2f", chanceToHit.avg))
        debug(String.format("avg duration:     %2.2f", averageDuration.avg))
        debug(String.format("avg total damage: %2.2f", averageTotalDamageOverTime.avg))
        debug()

        // TODO: resolve conflict between interest in
        //  (a) calc damage for each round individually
        //  (b) calc total damage across all rounds (some spells have multi-round impact)

        return AttackResult(
            numberOfTargets,
            chanceToHit,
            damagePerHit,
            averageDPR,
            averageDuration,
            averageTotalDamageOverTime
        )
    }

    // ==========================================================
    fun getMeleeOrRangeDPR(meleeOrRangeAttack: MeleeOrRangeAction, attack: Attack, monster: Monster, effectManager: EffectManager): AttackResult
    {
        // debug("\n##### getMeleeOrRangeDPR: $meleeOrRangeAttack")
        debug("\n##### getMeleeOrRangeDPR: "+attack.getLabel())

        val preconditions = attack.preconditions ?: Preconditions()
        val bonusDiceToHit = preconditions.bonusDiceToHit ?: DiceBlockHelper.emptyBlock()
        val penaltyDiceToHit = preconditions.penaltyDiceToHit ?: DiceBlockHelper.emptyBlock()
        val isBonusAction = attack.isBonusAction ?: false
        val bonusDamage   = meleeOrRangeAttack.getBonusDamage(character, isBonusAction) + (preconditions.bonusDamage ?: 0)
        debug()

        val attackBonus = meleeOrRangeAttack.getBonusToHit(character, isBonusAction)

        debug("target AC:     "+monster.properties.dataAcNum)
        debug("attack Bonus:  "+attackBonus)
        debug()

        // bonus DD is how we cover effects like Hunters Mark
        val bonusDamageDice = attack.preconditions?.bonusDamageDice ?: DiceBlock(0, 0, 0, 0, 0)

        val damageDice = meleeOrRangeAttack.getDamageDice().add(bonusDamageDice)

        debug("bonus damageDice: " + (attack.preconditions?.bonusDamageDice)) // include bonusDamageDice ...
        debug("meleeOrRange damage: " + damageDice) // include bonusDamageDice ...
        debug()

        val AC = monster.properties.dataAcNum

        val autoHit = 20 // for a champion, this could be 19 or 18      // E13
        val isElemental = character.isElementalAdept()

        // # Hit%:     (Y6, AC6, AG6) -> (B199, F199, J199)
        val chanceToHit = AvgMinMax(
            totalProb(attackBonus, AC,"No Advantage", isElemental, isLucky, autoHit, bonusDiceToHit,penaltyDiceToHit),
            totalProb(attackBonus, AC, "Advantage", isElemental, isLucky, autoHit, bonusDiceToHit, penaltyDiceToHit),
            totalProb(attackBonus, AC,"Disadvantage", isElemental, isLucky,autoHit, bonusDiceToHit, penaltyDiceToHit),
        )
        chanceToHit.debug("Chance to Hit")

        val mainAttack = !(attack.isBonusAction ?: false)
        debug(String.format("avg %%hit (%s):   %2.2f",  (if (mainAttack) "main" else "bonus"), chanceToHit.avg))

        // DPH:                             (B205, F205, J205)
        val damagePerHit = getAvgMinMax(damageDice, bonusDamage)
        damagePerHit.debug("Full Damage")

        // DPC (damage per crit)          (B208, F208, J208)
        val critDamage = getAvgMinMax(damageDice.double(), bonusDamage)
        critDamage.debug("Crit Damage")

        debug()

        // TODO: "autoCrit" from paralyzed/unconscious should only apply to melee attacks (not range)

        // Crit%:        (B211, F211, J211)
        val critChance = if (effectManager.isAutoCrit()) AvgMinMax(100f,100f,100f) else AvgMinMax(
            critChance(autoHit, "No Advantage", character.isElementalAdept(), isLucky),
            critChance(autoHit, "Advantage", character.isElementalAdept(), isLucky),
            critChance(autoHit, "Disadvantage", character.isElementalAdept(), isLucky),
        )
        critChance.debug("Crit %Hit")

        // Normal Attack DPR (does not include bonus attack):          (B202, F202, J202)

        // Melee or Range attack: 1 target per attack
        // (ignore 'numTargets' in user input, that only applies to spells with AOE impact)

        val numTargets = 1
        val attackDPR = AvgMinMax(
            numTargets * ((chanceToHit.avg - critChance.avg) * damagePerHit.avg + (critChance.avg * critDamage.avg)),
            numTargets * ((chanceToHit.max - critChance.max) * damagePerHit.max + (critChance.max * critDamage.max)),
            numTargets * ((chanceToHit.min - critChance.min) * damagePerHit.min + (critChance.min * critDamage.min)),
        )
        attackDPR.debug("Attack DPR (main="+mainAttack+")")

        /* Avg DPR:  (Y9, AC9, AG9) ->

                    Norm     BA                           BA    BA DPR  GWM     OncePerRound
                    DPR      Adv                          DPR   Select  Crit    DPR
                     *        *                            *              *       *
                    =B202+IF($G$42="Same as other attacks",Q202,$AD$202)+AJ211  +AJ199
                    =F202+IF($G$42="Same as other attacks",U202,$AD$202)+AN211  +AN199
                    =J202+IF($G$42="Same as other attacks",Y202,$AD$202)+AR211  +AR199
        */

        var result = AttackResult(numTargets, chanceToHit, damagePerHit, attackDPR, AvgMinMax(1f,1f,1f), attackDPR)
        result.attackerHadAdvantage = effectManager.attackerHasAdvantage() // stash this now, for future use
        return result
    }
}
