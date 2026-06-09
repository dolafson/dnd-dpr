package com.vikinghelmet.dnd.dpr.spells

import com.vikinghelmet.dnd.dpr.action.MeleeOrRangeAction
import com.vikinghelmet.dnd.dpr.action.enums.DamageType
import com.vikinghelmet.dnd.dpr.spells.payload.Attack
import com.vikinghelmet.dnd.dpr.spells.payload.Damage
import com.vikinghelmet.dnd.dpr.spells.payload.fields.AreaOfEffect
import com.vikinghelmet.dnd.dpr.util.DiceBlock
import dev.shivathapaa.logger.api.LoggerFactory
import kotlinx.serialization.Serializable
import kotlin.math.pow

val logger = LoggerFactory.get(SpellAttack::class.simpleName ?: "")

@Serializable
data class SpellAttack(
    val attackPayload: Attack,
    val damagePayload: Damage? = null,
    val attackBonus: Int ?= 0
) : MeleeOrRangeAction {
    private var numTargetsOverride: Int? = null

    constructor(other: SpellAttack, numTargets: Int, targetSpacing: Int) : this(other.attackPayload, other.damagePayload)
    {
        this.numTargetsOverride = other.getNumTargetsAffected(numTargets, targetSpacing) - 1
        other.numTargetsOverride = 1
    }

    fun getAoe(): AreaOfEffect? = attackPayload.aoe

    fun getAoeSize(): Int {
        return getAoe()?.getSizeInFeet() ?: 0
    }

    override fun getNumTargetsAffected(numTargets: Int, targetSpacing: Int): Int
    {
        if (numTargetsOverride != null) {
            return numTargetsOverride!!
        }

        if (attackPayload.aoe == null) {
            return 1
        }

        val size = getAoeSize()
        // for now, assume aoe is a square centered around a primary target
        // later we can try using aoe.shape

        //if (targetSpacing > size) return 1 // redundant?

        val numTargetsInArea = (1 + 2*(size / targetSpacing)).toDouble().pow(2.0).toInt()

        return kotlin.math.min (numTargets, numTargetsInArea)
    }

    override fun getDamageList(): List<com.vikinghelmet.dnd.dpr.action.Damage>
    {
        var damageType = damagePayload?.damageType ?: "undefined"
        if (damageType.contains(" ")) damageType = "fire" // choose acid, cold, fire, ...

        return listOf(com.vikinghelmet.dnd.dpr.action.Damage(
            damagePayload?.getDamageDice() ?: DiceBlock(),
            0, 0,
            DamageType.valueOf(damageType.lowercase())))
    }

    override fun getAttackBonus(): Int {
        return attackBonus ?: 0
    }

    // non-interface methods

    fun isMeleeOrRangeAttack() = (!isNoDamageAttack() && !isSavingThrowAttack())

    fun isNoDamageAttack(): Boolean {
        return (attackPayload.description == null && damagePayload == null) // TODO: find a cleaner way to represent this
    }

    fun isSavingThrowAttack() = (attackPayload.save != null)

    fun getSaveAbility() = attackPayload.save?.saveAbility


    fun getSaveResult(): SaveResult {
        val onSucceed = attackPayload.save?.onSucceed
        return if (onSucceed == null) {
            SaveResult.NOT_APPLICABLE
        }
        else if (".*three times.*".toRegex().matches(onSucceed)) {
            SaveResult.NOT_APPLICABLE  // TODO: accumulated saves
        }
        else if (".*[Hh]alf.*".toRegex().matches(onSucceed)) {
            SaveResult.HALF_DAMAGE
        }
        else if (".*([Ss]pell.*[Ee]nds|[Ee]nds.*[Ss]pell).*".toRegex().matches(onSucceed)) {
            SaveResult.SPELL_ENDS
        }
        else if (".*([Cc]ondition.*[Ee]nd|[Ee]nd.*[Ss]pell|no longer).*".toRegex().matches(onSucceed)) {
            SaveResult.CONDITION_ENDS
        }
        else if (".*([Nn]o damage|[Nn]o effect|unaffected|isn.t Restrained|resists your efforts|isn.t affected|none).*".toRegex().matches(onSucceed)) {
            SaveResult.NO_EFFECT
        }
        else {
            SaveResult.NOT_APPLICABLE
        }
    }

    override fun getActionName(): String {
        return attackPayload.name!!
    }
    override fun toString(): String {
        return attackPayload.name!!
    }
}