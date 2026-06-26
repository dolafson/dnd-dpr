package com.vikinghelmet.dnd.dpr.spells

import com.vikinghelmet.dnd.dpr.util.Globals

enum class SpellsWithComplexRules {

    Bane,
    BestowCurse,
    Bless,
    Blur,
    ChannelDivinity,
    ChannelDivinityPreserveLife,
    ChannelDivinityTurnUndead,
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
        return when (this) {
            ChannelDivinity             -> "Channel Divinity"
            ChannelDivinityPreserveLife -> "Channel Divinity: Preserve Life"
            ChannelDivinityTurnUndead   -> "Channel Divinity: Turn Undead"

            HuntersMark                 -> "Hunter's Mark"
            OttosIrresistibleDance      -> "Otto's Irresistible Dance"
            else ->  Globals.addWStoCamelCase(name)
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