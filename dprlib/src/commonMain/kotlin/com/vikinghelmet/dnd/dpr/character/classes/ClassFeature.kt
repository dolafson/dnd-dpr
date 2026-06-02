package com.vikinghelmet.dnd.dpr.character.classes

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ClassFeature {
    @SerialName("Subclass")                 Subclass,
    @SerialName("Divine Domain")            DivineDomain,
    @SerialName("Evasive")                  Evasive,
    @SerialName("Ability Score Improvement") AbilityScoreImprovement,
    @SerialName("Extra Attack")             ExtraAttack,
    @SerialName("Unarmored Defense")        UnarmoredDefense,
    @SerialName("Fighting Style")           FightingStyle,

    @SerialName("Dread Ambusher")           DreadAmbusher, // GloomStalker level 3
    ;

    fun getSerialName(): String {
        val descriptor = ClassFeature.serializer().descriptor
        return descriptor.getElementName(ordinal)
    }
}

