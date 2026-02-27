package com.vikinghelmet.dnd.dpr.spells

import com.vikinghelmet.dnd.dpr.spells.payload.Attack
import com.vikinghelmet.dnd.dpr.spells.payload.Damage
import com.vikinghelmet.dnd.dpr.util.DiceBlock
import com.vikinghelmet.dnd.dpr.util.DiceBlockHelper
import kotlinx.serialization.Serializable

@Serializable
data class SpellAttack(
    val attackPayload: Attack,
    val damagePayload: Damage? = null,
)
{
    fun isSavingThrowAttack(): Boolean {
        return attackPayload.save != null
    }

    fun getDamageDice(): DiceBlock {
        if (damagePayload == null) return DiceBlockHelper.emptyBlock()
        val diceCount = damagePayload.diceCount?: 1 // TODO: if absent, usually a cantrip that increases based on char level ...
        return DiceBlockHelper.getDiceBlock(""+diceCount+damagePayload.diceSize)
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