package com.vikinghelmet.dnd.dpr.character.actions

enum class ActionModifier(val nameWithWS: String) {
    HuntersLore("Hunter's Lore"),
    HordeBreaker("Horde Breaker"),
    ColossusSlayer("Colossus Slayer"),
    DreadfulStrike("Dreadful Strike"),
    PolarStrikes("Polar Strikes");

    companion object {
        fun fromName(name: String): ActionModifier? {
            return entries.firstOrNull { it.nameWithWS == name }
        }
    }
}