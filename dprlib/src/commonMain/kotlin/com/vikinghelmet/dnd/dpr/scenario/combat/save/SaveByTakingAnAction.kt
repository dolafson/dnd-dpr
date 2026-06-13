package com.vikinghelmet.dnd.dpr.scenario.combat.save

enum class SaveByTakingAnAction(val nameWithSpace: String) {
    DetectThoughts ("Detect Thoughts"),
    EnsnaringStrike ("Ensnaring Strike"),
    Entangle ("Entangle"),
    EvardsBlackTentacles ("Evard's Black Tentacles"),
    OtilukesFreezingSphere ("Otiluke's Freezing Sphere"),
    OttosIrresistibleDance ("Otto's Irresistible Dance"),
    Web ("Web"),
;
    companion object {
        fun contains(nameWithSpace: String): Boolean {
            return getByName(nameWithSpace) != null
        }

        fun getByName(nameWithSpace: String): SaveByTakingAnAction? {
            return entries.firstOrNull { it.nameWithSpace == nameWithSpace }
        }
    }
}