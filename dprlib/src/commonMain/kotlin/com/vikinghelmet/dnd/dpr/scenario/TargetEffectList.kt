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

    fun getDisadvantageOnSave(): AbilityType? = targetEffects.firstNotNullOfOrNull { it.disadvantageOnSave }
}