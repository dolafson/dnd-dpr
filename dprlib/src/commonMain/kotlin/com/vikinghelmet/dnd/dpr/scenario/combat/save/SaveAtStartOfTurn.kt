package com.vikinghelmet.dnd.dpr.scenario.combat.save

enum class SaveAtStartOfTurn(val nameWithSpace: String) {
    BestowCurse ("Bestow Curse"),
    Cloudkill ("Cloudkill"),
    ConjureElemental ("Conjure Elemental"),
    Earthquake ("Earthquake"),
    FaithfulHound ("Faithful Hound"),
    FingerOfDeath ("Finger of Death"),
    Heroism ("Heroism"),
    IncendiaryCloud ("Incendiary Cloud"),
    MordenkainensFaithfulHound ("Mordenkainen's Faithful Hound"),
    Regenerate ("Regenerate"),
    SearingSmite ("Searing Smite"),
    StinkingCloud ("Stinking Cloud"),
    StormOfVengeance ("Storm of Vengeance"),
;
    companion object {
        fun contains(nameWithSpace: String): Boolean {
            return getByName(nameWithSpace) != null
        }

        fun getByName(nameWithSpace: String): SaveAtStartOfTurn? {
            return entries.firstOrNull { it.nameWithSpace == nameWithSpace }
        }
    }
}