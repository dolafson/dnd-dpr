package com.vikinghelmet.dnd.dpr.scenario.combat.results

import com.vikinghelmet.dnd.dpr.action.Attack
import com.vikinghelmet.dnd.dpr.character.PlayerCharacter
import com.vikinghelmet.dnd.dpr.scenario.combat.CombatantWithStatus
import com.vikinghelmet.dnd.dpr.scenario.combat.location.Location
import com.vikinghelmet.dnd.dpr.scenario.combat.results.CombatActionResultField.*
import com.vikinghelmet.dnd.dpr.scenario.combat.save.HealthStatus
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
    val targetHealth: HealthStatus = HealthStatus.positive,
    val deathSaves: List<String>,

    val effects: String,
    val condition: String,

    val attackerNewLocation: Location,
) {
    @Transient
    private val logger = LoggerFactory.get(CombatActionResult::class.simpleName ?: "")

    constructor(c: CombatantWithStatus) : this(
        c, c, 0, "0", 0, "initiative = ${c.initiative}", emptyList(),
        c.getHP(), deathSaves = listOf(""), effects = "", condition = "", attackerNewLocation = c.location.copy()
    )

    constructor(
        combatant: CombatantWithStatus, target: CombatantWithStatus,
        turnId: Int, actionId: Int, effectId: Int,
        actionLabel: String
    ) : this(
        combatant, target,
        turnId, "$actionId", effectId,
        actionLabel, emptyList(),
        target.currentHP, target.healthStatus, toDeathSaves(target.deathSavingThrows),
        target.getEffectString(), target.getConditionString(), combatant.location.copy()
    )

    constructor(
        combatant: CombatantWithStatus, target: CombatantWithStatus,
        turnId: Int, actionId: Int, effectId: Int,
        attack: Attack, damageResultList: List<DamageResult>
    ) : this(
        combatant, target,
        turnId, (if (attack.isBonusAction == true) "BA" else "$actionId"), effectId,
        attack.getLabel(), damageResultList,
        target.currentHP, target.healthStatus, toDeathSaves(target.deathSavingThrows),
        target.getEffectString(), target.getConditionString(), combatant.location.copy()
    )

    fun getValue(field: CombatActionResultField): Any {
        return when (field) {
            // fields that do not vary across turns ...
            attackerName    -> attacker.getName()
            targetName      -> target.getName()
            battle           -> (attacker.combatant as? PlayerCharacter)?.getLevel() ?: 0
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

            attackerLocation -> this.attackerNewLocation
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