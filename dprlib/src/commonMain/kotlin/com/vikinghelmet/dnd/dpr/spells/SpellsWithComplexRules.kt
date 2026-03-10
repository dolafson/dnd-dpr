package com.vikinghelmet.dnd.dpr.spells

enum class SpellsWithComplexRules(val nameWithWS: String) {

    Bane("Bane"),
    BestowCurse("Bestow Curse"),
    Blur("Blur"),
    EnlargeReduce("Enlarge Reduce"),
    Enthrall("Enthrall"),
    FaerieFire("Faerie Fire"),
    GuidingBolt("Guiding Bolt"),
    Hex("Hex"),
    HuntersMark("Hunter's Mark"),
    MindSliver("Mind Sliver"),
    OttosIrresistibleDance("Otto's Irresistible Dance"),
    PhantasmalKiller("Phantasmal Killer"),
    RayOfEnfeeblement("Ray of Enfeeblement"),
    ShiningSmite("Shining Smite"),
    ViciousMockery("Vicious Mockery"),
    ;

    override fun toString(): String {
        return nameWithWS
    }

    companion object {
        fun fromNameWithWS(nameWithWS: String): SpellsWithComplexRules? {
            return entries.firstOrNull { it.nameWithWS.lowercase().startsWith(nameWithWS.lowercase()) }
        }
    }
}