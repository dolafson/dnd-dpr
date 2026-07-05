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

    // TODO: combine get(Disa|A)dvantageOnSave into one method that returns AttackAdvantage enum (tri-state) ?
    // TODO: (Disa|A)dvantageOnSave should return a list of AbilityType (eg Barbarian may have adv on STR and DEX)
    fun getDisadvantageOnSave(): AbilityType? = targetEffects.firstNotNullOfOrNull { it.disadvantageOnSave }
    fun getAdvantageOnSave(): AbilityType? = targetEffects.firstNotNullOfOrNull { it.advantageOnSave }
}