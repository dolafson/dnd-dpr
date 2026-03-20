package com.vikinghelmet.dnd.dpr.character.actions

import com.vikinghelmet.dnd.dpr.util.Globals

enum class ActionModifier {
    HuntersLore,
    HordeBreaker,
    ColossusSlayer,
    DreadfulStrike,
    PolarStrikes;

    fun getNameWithWS(): String {
        if (this == HuntersLore) return "Hunter's Lore"
        return Globals.addWStoCamelCase(name)
    }

    companion object {
        fun fromName(name: String): com.vikinghelmet.dnd.dpr.character.actions.ActionModifier? {
            return entries.firstOrNull { it.getNameWithWS() == name }
        }
    }
}