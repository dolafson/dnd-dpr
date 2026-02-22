@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.vikinghelmet.dnd.dpr.character

import com.vikinghelmet.dnd.dpr.character.feats.Feat
import com.vikinghelmet.dnd.dpr.character.modifiers.Modifier
import com.vikinghelmet.dnd.dpr.character.race.RacialTrait
import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class Character(
    @SerialName("data")
    val characterData: CharacterData,
    val id: Int? = null,
    val message: String? = null,
    val success: Boolean? = null
) {
    fun getRawAbilityScore(a: AbilityType): Int {
        for (stat in characterData.stats) {
            if (stat.id == a.ordinal) return stat.value
        }
        return 0 // should not get here
    }

    fun getBonusModifierSum(a: AbilityType, list: List<Modifier>): Int {
        var mod = 0
        for (modifier in list) {
            if (modifier.type == "bonus" && modifier.entityId == a.ordinal) {
                mod += (modifier.value?: 0)
            }
        }
        return mod
    }
    fun getModifiedAbilityScore(a: AbilityType): Int {
        return getRawAbilityScore(a) +
                getBonusModifierSum(a, characterData.modifiers.race) +
                getBonusModifierSum(a, characterData.modifiers.feat)
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

    fun test() {
        println ("")
        println (String.format("%-15s %-5s %s\n", "ability", "base", "withBonusesAdded"))

        for (ability in AbilityType.entries) {
            if (ability == AbilityType.unused) continue
            val base = getRawAbilityScore(ability)
            val withBonuses = getModifiedAbilityScore(ability)
            //println ("$ability: base=$base, withBonuses=$mod")
            println (String.format("  %-15s %3d %8d", ability, base, withBonuses))
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
