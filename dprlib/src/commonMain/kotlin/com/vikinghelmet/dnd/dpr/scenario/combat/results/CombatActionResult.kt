package com.vikinghelmet.dnd.dpr.scenario.combat.results

import com.vikinghelmet.dnd.dpr.action.Attack
import com.vikinghelmet.dnd.dpr.character.PlayerCharacter
import com.vikinghelmet.dnd.dpr.scenario.combat.CombatantWithStatus
import com.vikinghelmet.dnd.dpr.scenario.combat.results.CombatActionResultField.*
import com.vikinghelmet.dnd.dpr.util.Globals
import dev.shivathapaa.logger.api.LoggerFactory
import kotlinx.serialization.Transient

data class CombatActionResult(
    val attacker: CombatantWithStatus,
    val target: CombatantWithStatus,

    var turnId: Int = -1,
    var actionId: String = "",
    var effectId: Int = -1,

    val actionTaken: String,
    val damageResultList: List<DamageResult>,

    val targetHP: Int,
    val deathSaves: List<String>,

    val effects: String,
    val condition: String,
) {
    @Transient
    private val logger = LoggerFactory.get(CombatActionResult::class.simpleName ?: "")

    constructor(
        combatant: CombatantWithStatus, target: CombatantWithStatus,
        turnId: Int, actionId: Int, effectId: Int,
        attack: Attack, damageResultList: List<DamageResult>
    ) : this(combatant, target,
            turnId, (if (attack.isBonusAction == true) "BA" else "$actionId"), effectId,
            attack.getLabel(), damageResultList,
            target.currentHP, toDeathSaves(target.deathSavingThrows),
            target.getEffectString(), target.getConditionString())

    fun getValue(field: CombatActionResultField): Any {
        return when (field) {
            // fields that do not vary across turns ...
            attackerName    -> attacker.getName()
            targetName      -> target.getName()
            level           -> (attacker.combatant as? PlayerCharacter)?.getLevel() ?: 0
            //spellSaveDC   -> attacker.getSpellSaveDC()
            targetAC        -> target.getAC()

            // fields that vary
            turn        -> this.turnId
            action      -> this.actionId
            effect      -> this.effectId

            CombatActionResultField.actionTaken -> this.actionTaken
                // if (spellAttack != null) spellAttack.toString() else this.attack.getLabel()

            damageList  -> damageResultList

            CombatActionResultField.targetHP    -> this.targetHP
            CombatActionResultField.deathSaves  -> this.deathSaves

            endCondition -> Globals.wrapWithQuotes(this.condition)
            endEffects -> Globals.wrapWithQuotes(this.effects)
            /* else -> {
                println("WARNING: unhandled field: $field")
                Exception("warning").printStackTrace()
            } */
        }
    }

    override fun toString(): String {
        return "$attacker -> $target: damage=$damageResultList, attack=${actionTaken })"
    }

    companion object {
        fun toDeathSaves(list: List<Boolean>) = list.map { if (it) "pass" else "fail" }
    }
}