package com.vikinghelmet.dnd.dpr.scenario.combat

import com.vikinghelmet.dnd.dpr.action.Combatant

class CombatAction(
    val actor: Combatant,
    val turn: Int,
    val turnOrder: Int,
    val target: List<Combatant> = mutableListOf() // may be multiple targets for AOE spells
) {
}