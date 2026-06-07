package com.vikinghelmet.dnd.dpr.monsters

import com.vikinghelmet.dnd.dpr.monsters.actions.MonsterAction
import com.vikinghelmet.dnd.dpr.monsters.spells.Spellcasting
import kotlinx.serialization.Serializable

@Serializable
data class SpecialAbility(
    val spellcasting: Spellcasting?= null,
) : MonsterAction()