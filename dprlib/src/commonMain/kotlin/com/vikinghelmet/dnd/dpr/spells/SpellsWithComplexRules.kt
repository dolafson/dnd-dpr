package com.vikinghelmet.dnd.dpr.spells

import com.vikinghelmet.dnd.dpr.util.Globals

enum class SpellsWithComplexRules {

    Bane,
    BestowCurse,
    Bless,
    Blur,
    ChannelDivinity,  // only the derived forms of this can be cast, but we still need this here for filtering
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

    fun isChannelDivinity() = listOf(ChannelDivinityPreserveLife,ChannelDivinityTurnUndead).contains(this)

    override fun toString(): String {
        return when (this) {
            ChannelDivinityPreserveLife -> "Channel Divinity: Preserve Life"
            ChannelDivinityTurnUndead   -> "Channel Divinity: Turn Undead"

            HuntersMark                 -> "Hunter's Mark"
            OttosIrresistibleDance      -> "Otto's Irresistible Dance"
            else ->  Globals.addWStoCamelCase(name)
        }
    }

    companion object {
        fun fromName(nameWithWS: String) = entries.firstOrNull { it.toString() == nameWithWS }
    }
}