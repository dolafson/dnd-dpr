package com.vikinghelmet.dnd.dpr.scenario.combat

import com.vikinghelmet.dnd.dpr.action.Combatant
import com.vikinghelmet.dnd.dpr.spells.Spell

data class SpellCast(
    val caster: Combatant,
    val spell: Spell,
    val turnCast: Int,
    val turnEnded: Int? = null,
)

