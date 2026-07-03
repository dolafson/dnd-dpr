package com.vikinghelmet.dnd.dpr.scenario.combat

import com.vikinghelmet.dnd.dpr.action.Turn
import com.vikinghelmet.dnd.dpr.action.enums.DamageType
import com.vikinghelmet.dnd.dpr.scenario.combat.results.CombatActionResult
import com.vikinghelmet.dnd.dpr.scenario.combat.results.DamageResult
import com.vikinghelmet.dnd.dpr.spells.Spell
import dev.shivathapaa.logger.api.LoggerFactory
import kotlinx.serialization.Transient
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
class HealingAction(
    val combat: Combat,
    val combatant: CombatantWithStatus,
    val battleId: Int = combat.battleId,
    val turnId: Int = combat.turnId,
    var target: CombatantWithStatus = combatant // stop hitting yourself ... initialize to self because null is messier
)
{
    @Transient
    private val logger = LoggerFactory.get(HealingAction::class.simpleName ?: "")

    fun logInfo(msg: () -> String)  = logger.info  { combat.getAttrString() +": "+ msg() }

    fun takeAction(): List<CombatActionResult>
    {
        val healTarget = chooseHealingTarget() ?: return listOf(
            CombatActionResult(combatant, combatant, turnId, 0, 0,
                "goal is healing, but no healing target chosen" )
        )

        // move towards target, even if you can't heal them yet ...
        combatant.moveTowardTarget(healTarget, combat)

        if (combat.getOpponents(combatant).any { it.location == healTarget.location }) {
            return listOf(CombatActionResult(combatant, combatant, turnId, 0, 0,
                "unable to heal, opponent standing on healing target"))
        }

        // TODO: if selected healing target is not in range / unreachable (movement blocked), then either ...
        //  a) choose a closer healing target (if possible)
        //  b) perform a ranged weapon attack

        val range = combatant.distance(healTarget).toFeet()
        val healTurn = combatant.getPreferredTurn(ActionGoal.Heal, healTarget, range, combat)

        return if (healTurn != null) {
            healWithSpell(combatant, healTarget, healTurn.first)
        } else {
            listOf(CombatActionResult(combatant, combatant, turnId, 0, 0,
                "goal is healing, but no healing action available"))
        }
    }

    fun getMyTeam() = combat.getMyTeam (combatant)

    fun chooseHealingTarget(): CombatantWithStatus? {
        val team = getMyTeam()

        // if the team has a dying cleric, heal them first (so they can heal others)
        if (team.any { it.isCleric() && it.isDying() }) {
            return team.filter { it.isDying() }.maxBy { it.deathSavingThrows.count { false }}
        }

        // choose the one closest to death ... first the dying, then the stable
        if (team.any { it.isDying() }) {
            return team.filter { it.isDying() }.maxBy { it.deathSavingThrows.count { false }}
        }

        // if no one is dying, choose the closest stable patient (if any)
        if (team.any { it.isStable() }) {
            return team.filter { it.isStable() }.minByOrNull { it.distance(combatant) }
        }

        // if everyone is positive, ignore the undamaged, and heal someone with lowest HP
        return team.filter { it.isDamaged() }.minByOrNull { it.currentHP }
    }

    fun healWithSpell(healer: CombatantWithStatus, primaryTarget: CombatantWithStatus, turn: Turn): List<CombatActionResult> {
        val attack = turn.attacks.firstOrNull() ?: return emptyList()
        val spell = attack.action as? Spell ?: return emptyList()

        val targetsToHeal = if (spell.impactMultipleCreatures()) {
            getMyTeam().filter { !it.isDead() && it.getHP() > it.currentHP } // TODO: more selective healing targets
        } else {
            listOf(primaryTarget)
        }

        // TODO: filter healing targets by spell range

        val resultList = mutableListOf<CombatActionResult>()
        val healAmountRolled = healer.getHealingAmount(spell, true)

        var actionId = 0
        var effectId = 0

        for (healTarget in targetsToHeal) {
            var healAmount = healAmountRolled
            if (spell.impactMultipleCreatures()) {
                healAmount /= targetsToHeal.size    // TODO: support uneven distribution of healing amount
            }
            healTarget.applyHealing(healAmount)
            logInfo { "${healer.shortName()} heals ${healTarget.shortName()} for $healAmount HP (now ${healTarget.currentHP}/${healTarget.getHP()})" }

            val damageResultList = listOf(DamageResult(healAmount, DamageType.healing))
            resultList.add (CombatActionResult(
                healer,
                healTarget,
                turnId,
                actionId,
                effectId++,
                attack,
                damageResultList
            ))
        }

        healer.recordSpellCasting(spell, turnId, targetsToHeal)
        return resultList
    }
}
