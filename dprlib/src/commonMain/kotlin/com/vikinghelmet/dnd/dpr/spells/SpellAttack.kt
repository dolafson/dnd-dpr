package com.vikinghelmet.dnd.dpr.spells

import com.vikinghelmet.dnd.dpr.character.Character
import com.vikinghelmet.dnd.dpr.scenario.Scenario
import com.vikinghelmet.dnd.dpr.spells.payload.Attack
import com.vikinghelmet.dnd.dpr.spells.payload.Damage
import com.vikinghelmet.dnd.dpr.turn.MeleeOrRangeAction
import com.vikinghelmet.dnd.dpr.util.DiceBlock
import com.vikinghelmet.dnd.dpr.util.DiceBlockHelper
import kotlinx.serialization.Serializable
import kotlin.math.pow

@Serializable
data class SpellAttack(
    val attackPayload: Attack,
    val damagePayload: Damage? = null,
) : MeleeOrRangeAction {
    private var numTargetsOverride: Int? = null

    constructor(other: SpellAttack, scenario: Scenario) : this(other.attackPayload, other.damagePayload)
    {
        this.numTargetsOverride = other.getNumTargetsAffected(scenario) - 1
        other.numTargetsOverride = 1
    }

    fun getNumTargetsAffected(scenario: Scenario): Int
    {
        if (numTargetsOverride != null) {
            return numTargetsOverride!!
        }

        if (attackPayload.aoe == null) {
            return 1
        }

        /* aoe.size values, by frequency
            87  = "X"
            66  = "X foot"
            50  = "X foot radius"
            10  = "X foot radius, X foot high"
            10  = "X feet long, X feet high*"
            4  = "X-foot"
            4  = "X foot long"
            2  = "X foot tall, X foot radius"
            2  = "X feet wide, X feet long"
            1  = "X-foot-radius"
            1  = "X-foot-radius, X-foot-tall"
            1  = "X ft"
            1  = "X ft radius"
            1  = "X foot radius, X foot tall*"
            1  = "Ten X foot"
        */

        val aoe  = attackPayload.aoe
        val size = aoe.size.replace("Ten","10").replace("[ -].*".toRegex(), "").toInt()

        // for now, assume aoe is a square centered around a primary target
        // later we can try using aoe.shape

        //if (targetSpacing > size) return 1 // redundant?

        val numTargetsInArea = (1 + 2*(size / scenario.targetSpacing)).toDouble().pow(2.0).toInt()

        return kotlin.math.min (scenario.numTargets, numTargetsInArea)
    }

    override fun getBonusDamage(character: Character, isBonusAction: Boolean): Int {
        return 0
    }

    override fun getBonusToHit(character: Character, isBonusAction: Boolean): Int {
        return character.getSpellBonusToHit()
    }

    override fun getDamageDice(): DiceBlock {
        if (damagePayload == null) return DiceBlockHelper.emptyBlock()
        val diceCount = damagePayload.diceCount?: 1 // TODO: if absent, usually a cantrip that increases based on char level ...
        return DiceBlockHelper.get(""+diceCount+damagePayload.diceSize)
    }

    // non-interface methods

    fun isMeleeOrRangeAttack(): Boolean {
        return !isNoDamageAttack() && !isSavingThrowAttack()
    }

    fun isNoDamageAttack(): Boolean {
        return (attackPayload.description == null && damagePayload == null) // TODO: find a cleaner way to represent this
    }

    fun isSavingThrowAttack(): Boolean {
        return attackPayload.save != null
    }

    fun getSaveAbility(): String {
        return if (attackPayload.save == null) "" else  attackPayload.save.saveAbility
    }

    fun getSaveResult(): SaveResult {
        val onSucceed = attackPayload.save?.onSucceed
        return if (onSucceed == null) {
            SaveResult.NOT_APPLICABLE
        }
        else if (".*three times.*".toRegex().matches(onSucceed)) {
            SaveResult.NOT_APPLICABLE  // TODO: accumulated saves
        }
        else if (".*[Hh]alf.*amage.*".toRegex().matches(onSucceed)) {
            SaveResult.HALF_DAMAGE
        }
        else if (".*([Ss]pell.*[Ee]nds|[Ee]nds.*[Ss]pell).*".toRegex().matches(onSucceed)) {
            SaveResult.SPELL_ENDS
        }
        else if (".*([Cc]ondition.*[Ee]nd|[Ee]nd.*[Ss]pell|no longer).*".toRegex().matches(onSucceed)) {
            SaveResult.CONDITION_ENDS
        }
        else if (".*([Nn]o damage|[Nn]o effect|unaffected|isn.t Restrained|resists your efforts|isn.t affected).*".toRegex().matches(onSucceed)) {
            SaveResult.NO_EFFECT
        }
        else {
            SaveResult.NOT_APPLICABLE
        }
    }

    fun isAreaOfEffectBig(): Boolean {
        //   AreaOfEffect(var shape: AreaOfEffectShape, var size: String)
        // println("isBig, size = "+size)

        // first field in size is almost always numeric; for now, treat 2-digit size as big
        //return "[0-9][0-9].*".toRegex().matches(size)

        // for now, always true ... ?
        return attackPayload.aoe != null // TODO
    }

    override fun toString(): String {
        return attackPayload.name!!
    }
}