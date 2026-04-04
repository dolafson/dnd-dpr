package com.vikinghelmet.dnd.dpr.character.actions

import com.vikinghelmet.dnd.dpr.util.Globals

enum class ActionModifier {

    BreathWeapon,
    HuntersLore,
    HordeBreaker,
    ColossusSlayer,
    DreadfulStrike,
    PolarStrikes,

    ExtraAttack,

    Cleave, // not sure yet if this belongs here ... looking for a way to make this extra attack
            // rationale visible in attack results ... see WeaponMastery.Cleave for details
    ;

    fun getNameWithWS(): String {
        if (this == HuntersLore) return "Hunter's Lore"
        return Globals.addWStoCamelCase(name)
    }

    companion object {
        fun fromName(name: String): com.vikinghelmet.dnd.dpr.character.actions.ActionModifier? {
            return entries.firstOrNull { it.getNameWithWS() == name }
        }
        fun partialMatch(name: String): com.vikinghelmet.dnd.dpr.character.actions.ActionModifier? {
            return entries.firstOrNull { name.startsWith(it.getNameWithWS()) }
        }
    }
}