package com.vikinghelmet.dnd.dpr.scenario.combat.save

enum class SaveAtEndOfTurn(val nameWithSpace: String) {

    BlindnessDeafness ("Blindness Deafness"),
    Confusion ("Confusion"),
    Contagion ("Contagion"),
    Eyebite ("Eyebite"),
    FleshToStone ("Flesh to Stone"),
    HideousLaughter ("Hideous Laughter"),
    HoldMonster ("Hold Monster"),
    HoldPerson ("Hold Person"),
    PhantasmalKiller ("Phantasmal Killer"),
    PowerWordStun ("Power Word Stun"),
    RayOfEnfeeblement ("Ray of Enfeeblement"),
    Slow ("Slow"),
    Sunburst ("Sunburst"),
    TashasHideousLaughter ("Tasha's Hideous Laughter"),
    Weird ("Weird"),
;
    companion object {
        fun contains(nameWithSpace: String): Boolean {
            return getByName(nameWithSpace) != null
        }

        fun getByName(nameWithSpace: String): SaveAtEndOfTurn? {
            return entries.firstOrNull { it.nameWithSpace == nameWithSpace }
        }
    }
}