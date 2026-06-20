package com.vikinghelmet.dnd.dpr.scenario.combat

import com.vikinghelmet.dnd.dpr.character.PlayerCharacter
import com.vikinghelmet.dnd.dpr.character.classes.ClassName
import com.vikinghelmet.dnd.dpr.scenario.combat.TargetSelectionStrategy.*
import com.vikinghelmet.dnd.dpr.scenario.combat.location.Distance
import dev.shivathapaa.logger.api.LoggerFactory
import kotlinx.serialization.Transient

data class TargetSelector(
    val combat: Combat,
    val combatant: CombatantWithStatus,
    val targetList: List<CombatantWithStatus>
) {
    @Transient
    val logger = LoggerFactory.get(TargetSelector::class.simpleName ?: "")

    val attackResultList = combat.actionResultList
    val team = (if (combatant.onTeamA) combat.teamA else combat.teamB)

    fun getTargetAttackingFriendWhoIsAlmostDead() : CombatantWithStatus?
    {
        // find damagedFriend with minimal HP and distance
        val damagedFriend = team.filter { friend -> friend != combatant && friend.currentHP < friend.getHP() / 2 }
            .minByOrNull { it.currentHP * combatant.distance(it).toFeet() } ?: return null

        return targetList.filter { it.target == damagedFriend }
                .filter { combatant.distance(it) < Distance.fromFeet(30) }  // can we get to them in time ?
                .firstOrNull()
    }

    fun getTargetWithHighDamageToAttacker() : CombatantWithStatus? {
        return attackResultList
            .filter { this.targetList.contains(it.attacker) }
//            .filter { it.targetList.any { it2 -> it2.onTeamA != combatant.onTeamA } } // opposing team
            .filter { it.target == this.combatant } // damaged you personally
            .groupBy { it.attacker }
            .mapValues { entry -> entry.value.sumOf { it.damageResultList.sumOf { it.amount } } }
            .toList()
            .filter { it.second > this.combatant.getHP() / 4 }
            .maxByOrNull { it.second }
            ?.first
    }

    fun getTargetWithHighDamageToTeam() : CombatantWithStatus? {
        return attackResultList
            .filter { this.targetList.contains(it.attacker) }
//            .filter { it.targetList.any { it2 -> it2.onTeamA != combatant.onTeamA } } // opposing team
            .groupBy { it.attacker }
            .mapValues { entry -> entry.value.sumOf { it.damageResultList.sumOf { it.amount } } }
            .toList()
            .filter { it.second > this.combatant.getHP() / 2 }
            .maxByOrNull { it.second }
            ?.first
    }

    fun getBiggestTarget() : CombatantWithStatus? {
        // this strategy should be avoided by healers
        if (combatant.combatant is PlayerCharacter && combatant.combatant.getClass() == ClassName.Cleric) {
            return null
        }
        return targetList.maxByOrNull { it.getHP() }
    }

    fun getEasiestTarget() : CombatantWithStatus? {
        return targetList.minByOrNull { it.getAC() }
    }

    fun getClosestTarget() : CombatantWithStatus? {
        return targetList.minByOrNull { it.distance(combatant.location) }
    }

    fun select() : Pair<CombatantWithStatus?, TargetSelectionStrategy>
    {
        var result: Pair<CombatantWithStatus?, TargetSelectionStrategy>? = null

        // pursue strategies in enum order
        for (strategy in TargetSelectionStrategy.entries) {
            val target = when (strategy) {
                targetAttackingFriendWhoIsAlmostDead -> getTargetAttackingFriendWhoIsAlmostDead()
                targetWithHighDamageToAttacker       -> getTargetWithHighDamageToAttacker()
                targetWithHighDamageToTeam           -> getTargetWithHighDamageToTeam()
                biggestTarget                        -> getBiggestTarget()
                easiestTarget                        -> getEasiestTarget()
                // noTarget // TODO: figure out where this fits; this might make sense for a cleric
                closestTarget                        -> getClosestTarget()
                else -> null
            }

            if (target != null) {
                result = Pair(target, strategy)
                break
            }
        }

        if (result != null) {
            logger.debug { "combatant=$combatant, ${result.second.name} = ${result.first}" }
            return result
        }

        return Pair(null, noTarget)
    }
}