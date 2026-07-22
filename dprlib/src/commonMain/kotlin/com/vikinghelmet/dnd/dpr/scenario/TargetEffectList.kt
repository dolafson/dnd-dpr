package com.vikinghelmet.dnd.dpr.scenario

import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import com.vikinghelmet.dnd.dpr.util.AttackAdvantage

open class TargetEffectList(
    private val targetEffects: MutableList<TargetEffect> = mutableListOf()
)  : MutableList<TargetEffect> by targetEffects {

    fun getAttacksAgainstMe(): AttackAdvantage {
        return AttackAdvantage.fromList (targetEffects.map { it.attacksAgainstMe }.toList())
    }

    fun getAttacksAgainstOthers(): AttackAdvantage {
        return AttackAdvantage.fromList (targetEffects.map { it.attacksAgainstOthers }.toList())
    }

    fun isAttackerAutoCritDamage(): Boolean = targetEffects.any { it.attackerAutoCritDamage }
    fun isAutoFailStrAndDexSaves(): Boolean = targetEffects.any { it.autoFailStrAndDexSaves }
    fun isUnableToAct():            Boolean = targetEffects.any { it.unableToAct }

    fun getAdvantageOnSave(abilityType: AbilityType): AttackAdvantage {
        val adv    = targetEffects.any { it.advantageOnSave    == abilityType }
        val disadv = targetEffects.any { it.disadvantageOnSave == abilityType }
        return if (adv && !disadv) AttackAdvantage.advantage
            else if (!adv && disadv) AttackAdvantage.disadvantage
            else AttackAdvantage.normal
    }
}