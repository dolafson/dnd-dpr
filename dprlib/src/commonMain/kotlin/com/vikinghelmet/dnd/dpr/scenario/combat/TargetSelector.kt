package com.vikinghelmet.dnd.dpr.scenario.combat

import com.vikinghelmet.dnd.dpr.character.PlayerCharacter
import com.vikinghelmet.dnd.dpr.character.classes.ClassName
import com.vikinghelmet.dnd.dpr.scenario.combat.TargetSelectionStrategy.*

data class TargetSelector(
    val combat: Combat,
    val combatant: CombatantWithStatus,
    val targetList: List<CombatantWithStatus>
) {
    val attackResultList = combat.attackResultList
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
            .filter { it.targetList.contains(combatant) } // damaged you personally
            .groupBy { it.combatant }
            .mapValues { entry -> entry.value.sumOf { it.totalDamage } }
            .toList()
            .filter { it.second > combatant.getHP() / 4 }
            .maxByOrNull { it.second }
            ?.first
    }

    fun getTargetWithHighDamageToTeam() : CombatantWithStatus? {
        return attackResultList
            .groupBy { it.combatant }
            .mapValues { entry -> entry.value.sumOf { it.totalDamage } }
            .toList()
            .filter { it.second > combatant.getHP() / 2 }
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
                return Pair(target, strategy)
            }
        }

        return Pair(null, noTarget)
    }
}