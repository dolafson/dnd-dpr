package com.vikinghelmet.dnd.dpr.character.feats

import com.vikinghelmet.dnd.dpr.character.PlayerCharacter
import com.vikinghelmet.dnd.dpr.character.classes.ClassName.*

object FeatEligibility {
    fun getListByCharacter(playerCharacter: PlayerCharacter): List<Feat> {
        return Feat.entries.filter {
            (it.asiPrerequisite.isEmpty() || it.asiPrerequisite.any { p -> playerCharacter.getAbilityScore(p) >= 13 } )
                    &&
            (! it.isFightingStyle || listOf(Fighter, Paladin, Ranger).contains(playerCharacter.getClass()))
        }
    }
}