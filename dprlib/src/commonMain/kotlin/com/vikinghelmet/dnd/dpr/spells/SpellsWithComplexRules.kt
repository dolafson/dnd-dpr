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
    Grease,
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

    fun getNameWithWS(): String {   // TODO: make this private
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
        fun fromName(nameWithWS: String) = entries.firstOrNull { it.getNameWithWS() == nameWithWS }
    }
}