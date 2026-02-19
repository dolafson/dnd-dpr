package com.vikinghelmet.dnd.dpr
import kotlin.math.max
import kotlin.math.min

data class AvgMinMax(var avg: Float, var min: Float, var max: Float) {
    fun half(diceBlock: DiceBlock): AvgMinMax {
        val halfAvg = if (diceBlock.empty()) avg/2 else avg/2 - 0.25f
        return AvgMinMax(halfAvg, (min.toInt() / 2).toFloat(), (max.toInt() / 2).toFloat())
    }
    fun print(label: String) {
        println("$label, (avg, min, max) = ($avg, $min, $max)")
    }
}

class DamagePerRound (
    var targetSaveBonus: Int,
    var effectSaveDC: Int,
    var numberOfEfectsOrTargets: Int,
    var maxDurationInRounds: Int,
    var isLucky: Boolean,
    var damageDice: DiceBlock,
    var damageBonus: Int,
    var bonusDiceToSave: DiceBlock,
    var penaltyDiceToSave: DiceBlock,
) {

    fun getAvgMinMax(diceBlock: DiceBlock, bonusDamage: Int): AvgMinMax {
        var avg = averageDamage(diceBlock, bonusDamage, false, false)
        var min = diceBlock.min()+bonusDamage.toFloat()
        var max = diceBlock.max()+bonusDamage.toFloat()
        return AvgMinMax(avg, min, max)
    }

//    https://docs.google.com/document/d/11eTMZPPxWXHY0rQEhK1msO-40BcCGrzArSl4GX4CiJE/edit?tab=t.0

// Base probability of hitting a given AC with a given attack bonus.
    fun toHit(atk: Int, ac: Int, autohit: Int): Float {
        return max(min(21 - ac + atk, 19), 21 - autohit)  /  20f;
    }

// Probability of a hit with the Halfling Luck feature:
// that is, the probability of a hit, or a 1 becoming a hit.
    fun luckP(p: Float, isLucky: Boolean): Float
    {
        if (isLucky) {
            return p * (1 + 1 / 20);
        }
        return p;
    }

// Transform a base hit probability to include disadvantage
// and (if applicable) Halfling Luck.
    fun disadvP(p: Float, isLucky: Boolean): Float
    {
        // Disadvantage transform:
        // You have to hit with both dice.
        var d = p * p;

        // Lucky transform:
        // If you get a 1 on either die, you missed, but you get
        // another shot.
        if (isLucky) {
            d += 2 * p * p / 20;
        }
        return d;
    }

// Transform a base hit probability to include advantage
// and (if applicable) Halfling Luck.
    fun advanP(p: Float, isLucky: Boolean): Float
    {
        var a = 2 * p - p * p;
        if (isLucky) {
            a += (2 / 20 * (1 - p) - 1 / 400) * p;
        }
        return a;
    }

// Transform a base hit probability to include Elven Accuracy
// and (if applicable, though it's difficult to imagine why it
// would be) Halfling Luck.
    fun elvenP(p: Float, isLucky: Boolean): Float
    {
        var e = p * p * p + 3 * p - 3 * p * p;
        if (isLucky) {
            e += (3 * (1 - p) * (1 - p) / 20 - 3 * (1 - p) / 400 + 1 / 8000) * p;
        }
        return e;
    }

// Utility fun: takes the probability distribution of a value X
// and computes the probability distribution of X + D (for a single
// die of size D).
    fun convolve(arr: MutableList<Float>, die: Int)
    {
        for (i in 1..die) {
            arr.add(0f)
        }

        for (j in arr.size-1 downTo 0) {
            arr[j] = 0f;
            for (k in j-1 downTo (j - die)){
                if (k < 0) break
                arr[j] += arr[k];
            }
            //arr[j] /= die;
            arr[j] = arr[j] / die;
        }
    }

// Compute the probability distribution of a pile of
// d4, d6, d8, d10, and d12 rolls.
    fun buffDist(diceBlock: DiceBlock): List<Float>
//        four: Int, six: Int, eight: Int, ten: Int, twelve: Int): List<Float>
    {
        var arr: MutableList<Float> = mutableListOf(1f);

        for (i in 0..<diceBlock.four) {
            convolve(arr, 4);
        }
        for (i in 0..<diceBlock.six) {
            convolve(arr, 6);
        }
        for (i in 0..<diceBlock.eight) {
            convolve(arr, 8);
        }
        for (i in 0..<diceBlock.ten) {
            convolve(arr, 10);
        }
        for (i in 0..<diceBlock.twelve) {
            convolve(arr, 12);
        }
        return arr;
    }

// Compute the attack probability taking all variables into account:
// attack bonus, AC, advantage, Elven Accuracy, and bonus dice (e.g. Bless).
    fun totalProb(atk: Int, ac: Int, isAdvan: String, isElven: Boolean, isLucky: Boolean,
                  autohit: Int): Float
    {
        var arr    = buffDist(bonusDiceToSave)
        var negArr = buffDist(penaltyDiceToSave)

        var blessed: Float = 0f
        var foo: Float = 1 * 2 * luckP(1f, true)

        if (isAdvan == "No Advantage") {
            for (i in 0..<arr.size) {
                for (j in 0..<negArr.size) {
                    var inc: Float = (0f)
                    blessed += (arr[i] * negArr[j] * luckP(toHit(atk + i - j, ac, autohit), isLucky));
                }
            }
        }
        if (isAdvan == "Advantage" && isElven) {
            for (i in 0..<arr.size) {
                for (j in 0..<negArr.size) {
                    blessed += (arr[i] * negArr[j] * elvenP(toHit(atk + i - j, ac, autohit), isLucky));
                }
            }
        }
        if (isAdvan == "Advantage" && !isElven) {
            for (i in 0..<arr.size) {
                for (j in 0..<negArr.size) {
                    blessed += (arr[i] * negArr[j] * advanP(toHit(atk + i - j, ac, autohit), isLucky));
                }
            }
        }
        if (isAdvan == "Disadvantage") {
            for (i in 0..<arr.size) {
                for (j in 0..<negArr.size) {
                    blessed += (arr[i] * negArr[j] * disadvP(toHit(atk + i - j, ac, autohit), isLucky));
                }
            }
        }

        return blessed;
    }

// Compute the crit chance given the relevant parameters:
// the crit threshold (should normally be 20, except for things
// like Champion features), and the presence of Advantage,
// Elven Accuracy, and Halfling Luck.
    fun critChance(threshold: Int, isAdvan: String, isElven: Boolean, isLucky: Boolean): Float
    {
        if (threshold > 20 || threshold < 1) {
            return 0f;
        }
        var p = (21 - threshold) / 20f;
        var c: Float = 0f
        if (isAdvan == "No Advantage") {
            c = luckP(p, isLucky);
        }
        if (isAdvan == "Advantage" && !isElven) {
            c = advanP(p, isLucky);
        }
        if (isAdvan == "Advantage" && isElven) {
            c = elvenP(p, isLucky);
        }
        if (isAdvan == "Disadvantage") {
            c = disadvP(p, isLucky);
        }

        return c;
    }

// **********************************************************************
// The following section focuses on computing SAVE probabilities,
// which have the notable difference that there are no crits.

// Compute the base save probability based on bonus and DC.
    fun toSave(bonus: Int, dc: Int): Float
    {
        return max(min(21 - dc + bonus, 20), 0) / 20f;
    }

// Probability of a hit with the Halfling Luck feature:
// that is, the probability of a hit, or a 1 becoming a hit.
    fun luckS(p: Float, isLucky: Boolean): Float
    {
        if (isLucky) {
            if (p == 1f) {
                return p;
            } else {
                return p * (1 + 1 / 20f);
            }
        }
        return p;
    }

// Transform a base hit probability to include disadvantage
// and (if applicable) Halfling Luck.
    fun disadvS(p: Float, isLucky: Boolean): Float
    {
        // Disadvantage transform:
        // You have to hit with both dice.
        var d = p * p;

        // Lucky transform:
        // If you get a 1 on either die and you missed,
        // you get another shot.
        if (isLucky) {
            if (p == 1f) {
                return p;
            } else {
                d += 2 * p * p / 20f;
            }
        }
        return d;
    }

// Transform a base hit probability to include advantage
// and (if applicable) Halfling Luck.
    fun advanS(p: Float, isLucky: Boolean): Float
    {
        var a = 2 * p - p * p;
        if (isLucky) {
            if (p == 1f) {
                return p;
            } else {
                a += (2 / 20f * (1 - p) - 1 / 400f) * p;
            }
        }
        return a;
    }


// Compute the save probability taking all variables into account:
// save bonus, DC, advantage, and bonus dice (e.g. Bless).
    fun saveProb(isAdvan: String): Float
    {
        var bonus: Int = targetSaveBonus
        var dc: Int = effectSaveDC

        var arr    = buffDist(bonusDiceToSave)
        var negArr = buffDist(penaltyDiceToSave)

        var blessed = 0f;
/*
        println("four: $four, six: $six, eight: $eight, ten: $ten, twelve: $twelve")
        println("mfour: $mfour, msix: $msix, meight: $meight, mten: $mten, mtwelve: $mtwelve")

        println("arr = "+arr)
        println("negArr = "+negArr)
*/

        if (isAdvan == "No Advantage") {
            for (i in 0..<arr.size) {
                for (j in 0..<negArr.size) {
                    blessed += (arr[i] * negArr[j] * luckS(toSave(bonus + i - j, dc), isLucky));
                }
            }
        }
        if (isAdvan == "Advantage") {
            for (i in 0..<arr.size) {
                for (j in 0..<negArr.size) {
                    blessed += (arr[i] * negArr[j] * advanS(toSave(bonus + i - j, dc), isLucky));
                }
            }
        }
        if (isAdvan == "Disadvantage") {
            for (i in 0..<arr.size) {
                for (j in 0..<negArr.size) {
                    blessed += (arr[i] * negArr[j] * disadvS(toSave(bonus + i - j, dc), isLucky));
                }
            }
        }

        return blessed;
    }

// Compute the average DPR of a single attack.
// Takes into account the chance of a hit, the damage from a hit,
// the chance of a crit, and the damage on a crit (INCLUDING the damage
// already dealt by an ordinary hit).
    fun attackDPR(hitChance: Float, hitDamage: Float, critChance: Float, critDamage: Float): Float
    {
        return (hitChance - critChance) * hitDamage + critChance * critDamage;
    }

    //class FloatBlock (four: Float, six: Float, eight: Float, ten: Float, twelve: Float)

// Compute the average output of a series of dice with a bonus.
// The last two arguments are booleans to indicate whether the features
// Great Weapon Fighting (Fighting Style) and Elemental Adept (Feat)
// are active.
    fun averageDamage(diceBlock: DiceBlock, bonusDamage: Int, isGWF: Boolean, isEA: Boolean): Float
    {
        var floatList: MutableList<Float> = mutableListOf(0f)

        if (isGWF) {
            if (isEA) {
                floatList = mutableListOf(3.125f, (38f / 9), (169f / 32), 6.32f, (529f / 72))
            } else {
                floatList = mutableListOf(3f, (25f / 6), 5.25f, 6.3f, (22f / 3))
            }
        } else {
            if (isEA) {
                floatList = mutableListOf(2.75f, (11f / 3), 4.625f, 5.6f, (79f / 12))
            } else {
                floatList = mutableListOf(2.5f, 3.5f, 4.5f, 5.5f, 6.5f)
            }
        }

        var sum: Float = bonusDamage + 0f
        var diceList = diceBlock.toList()

        for (i in 0..<floatList.size) {
            sum += floatList[i] * diceList[i]
        }
        return sum
    }

    fun minDamage(diceBlock: DiceBlock): Int {
        return damageBonus + damageDice.min()
    }

    fun maxDamage(diceBlock: DiceBlock): Int {
        return damageBonus + damageDice.max()
    }

}
