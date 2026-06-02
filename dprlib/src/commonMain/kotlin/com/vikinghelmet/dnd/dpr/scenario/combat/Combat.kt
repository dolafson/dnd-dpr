package com.vikinghelmet.dnd.dpr.scenario.combat

import com.vikinghelmet.dnd.dpr.action.Combatant

data class Combat(
    val teamA: List<Combatant>,
    val teamB: List<Combatant>,
)
{
    var initiativeList = listOf<Combatant>()
    val combatantStatusMap = mutableMapOf<Combatant, CombatantStatus>()
    val combatActionList = mutableListOf<CombatAction>()

    fun start() {
        fun randomX() = (1..4).random()
        fun randomY() = (-2..2).random()
        teamA.forEach { combatantStatusMap[it] = CombatantStatus(it, true,  0, Location(-1 * randomX(), randomY()), 0) }
        teamB.forEach { combatantStatusMap[it] = CombatantStatus(it, false, 0, Location(randomX(), randomY()), 0) }

        // when computing initiative order, a DM will often use a single roll for an entire group of monsters
        // we won't do that here - unique rolls are more realistic, and easier to implement

        val initiativeMap = mutableMapOf<Combatant, Int>()
        teamA.forEach { initiativeMap[it] = (1..20).random() + it.getInitiativeBonus() }
        teamB.forEach { initiativeMap[it] = (1..20).random() + it.getInitiativeBonus() }

        initiativeList = initiativeMap.entries.sortedBy { it.value }.map { it.key }.toList()
    }

    fun fullTurn() {
        initiativeList.forEach {
            if (combatantStatusMap[it]!!.isAlive()) {
                takeTurn(it)
            }
        }
    }

    fun takeTurn(combatant: Combatant) {
        // choose target and action
        // if not within attack range, move
        // if successful, modify target status
        // if not within desired post-attack range, move
    }
}
