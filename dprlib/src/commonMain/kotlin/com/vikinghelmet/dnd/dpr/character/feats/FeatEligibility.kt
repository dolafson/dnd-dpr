package com.vikinghelmet.dnd.dpr.character.feats

import com.vikinghelmet.dnd.dpr.character.Character
import com.vikinghelmet.dnd.dpr.character.classes.ClassName.*

object FeatEligibility {
    fun getListByCharacter(character: Character): List<Feat> {
        return Feat.entries.filter {
            (it.asiPrerequisite.isEmpty() || it.asiPrerequisite.any { p -> character.getModifiedAbilityScore(p) >= 13 } )
                    &&
            (! it.isFightingStyle || listOf(Fighter, Paladin, Ranger).contains(character.getClass()))
        }
    }
}