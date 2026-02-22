@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.vikinghelmet.dnd.dpr.character

import com.vikinghelmet.dnd.dpr.character.abilities.AbilityType
import com.vikinghelmet.dnd.dpr.character.feats.Feat
import com.vikinghelmet.dnd.dpr.character.race.RacialTrait
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class Character(
    @SerialName("data")
    val characterData: CharacterData,
    val id: Int,
    val message: String,
    val success: Boolean
) {
    fun getRawAbilityScore(a: AbilityType): Int {
        for (stat in characterData.stats) {
            if (stat.id == a.ordinal) return stat.value
        }
        return 0 // should not get here
    }

    fun getModifiedAbilityScore(a: AbilityType): Int {
        val raw = getRawAbilityScore(a)
        var mod = 0
        for (modifier in characterData.modifiers.race) {
            if (modifier.type == "bonus" && modifier.entityId == a.ordinal) {
                mod = (modifier.value?: 0)
                break
            }
        }
        return raw + mod
    }

    fun getLevel(): Int {
        return characterData.classes.first().level
    }

    fun getProficiencyBonus(): Int {
        return Constants.levelToProficiencyMap[getLevel()] ?: 0
    }

    fun getSpellSaveDC(): Int {
        val abilityId = characterData.classes.first().definition.spellCastingAbilityId
        val statBonus = if (abilityId == null) 0 else {
            Constants.statToBonusMap[getModifiedAbilityScore(AbilityType.entries[abilityId])] ?: 0
        }
        return 8 + statBonus + getProficiencyBonus()
    }

    fun isFeatEnabled(requested : Feat): Boolean {
        for (feat in characterData.feats) {
            if (feat.definition.name == requested.traitName) return true
        }
        return false
    }

    fun isRacialTraitEnabled(requested : RacialTrait): Boolean {
        for (trait in characterData.race.racialTraits) {
            if (trait.definition.name == requested.traitName) return true
        }
        return false
    }

    fun isLucky(): Boolean {
        return isRacialTraitEnabled (RacialTrait.Luck)
    }

    fun isElvenAccuracy(): Boolean {
        return isRacialTraitEnabled (RacialTrait.ElvenAccuracy)
    }

    fun isGreatWeaponFighting(): Boolean {
        return isFeatEnabled(Feat.GreatWeaponFighting)
    }

    fun dump() {
        println ("")
        for (ability in AbilityType.entries) {
            if (ability == AbilityType.unused) continue
            val raw = getRawAbilityScore(ability)
            val mod = getModifiedAbilityScore(ability)
            println ("$ability: raw=$raw, mod=$mod")
        }

        val abilityId = characterData.classes.first().definition.spellCastingAbilityId
        val spellAbilityType = if (abilityId == null) "n/a" else AbilityType.entries[abilityId]

        println ("")
        println ("level         = "+getLevel())
        println ("PB            = "+getProficiencyBonus())
        println ("spell ability = "+spellAbilityType)
        println ("spellSaveDC   = "+getSpellSaveDC())
        println ("")
        println ("isLucky       = "+isLucky())
        println ("isEA          = "+isElvenAccuracy())
        println ("isGWF         = "+isGreatWeaponFighting())

        println ("")
        for (trait in characterData.race.racialTraits) {
            println("racial trait: "+trait.definition.name)
        }

        println ("")
        for (feat in characterData.feats) {
            println("feat: "+feat.definition.name)
        }
        println ("")
    }
}
