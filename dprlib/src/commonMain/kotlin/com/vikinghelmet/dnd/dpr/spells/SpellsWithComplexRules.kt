package com.vikinghelmet.dnd.dpr.spells

import com.vikinghelmet.dnd.dpr.util.Globals

enum class SpellsWithComplexRules {

    Bane,
    BestowCurse,
    Blur,
    EnlargeReduce,
    Enthrall,
    FaerieFire,
    GuidingBolt,
    Hex,
    HuntersMark,
    MindSliver,
    OttosIrresistibleDance,
    PhantasmalKiller,
    RayOfEnfeeblement,
    ShiningSmite,
    ViciousMockery,
    WindWall,
    ;

    fun getNameWithWS(): String {
        when (this) {
            HuntersMark -> return "Hunter's Mark"
            OttosIrresistibleDance -> return "Otto's Irresistible Dance"
            else -> return Globals.addWStoCamelCase(name)
        }
    }

    override fun toString(): String {
        return getNameWithWS()
    }

    companion object {
        fun fromNameWithWS(nameWithWS: String): SpellsWithComplexRules? {
            return entries.firstOrNull { it.getNameWithWS().lowercase().startsWith(nameWithWS.lowercase()) }
        }
    }
}