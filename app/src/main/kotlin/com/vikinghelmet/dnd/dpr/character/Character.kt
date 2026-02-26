@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.vikinghelmet.dnd.dpr.character

import com.vikinghelmet.dnd.dpr.character.feats.Feat
import com.vikinghelmet.dnd.dpr.character.inventory.Weapon
import com.vikinghelmet.dnd.dpr.character.modifiers.Modifier
import com.vikinghelmet.dnd.dpr.character.race.RacialTrait
import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import com.vikinghelmet.dnd.dpr.util.Constants
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
    fun is2014(): Boolean {
        if (characterData.characterValues == null) return false // default to 2024 rules
        for (value in characterData.characterValues) {
            if (value.notes?.endsWith(" 2014") == true) return true
        }
        return false
    }

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

    /**
     * Elemental Adept in D&D 5e enhances damage by treating any 1 rolled on damage dice for a chosen element
     * (acid, cold, fire, lightning, or thunder) as a 2. It also allows spells to ignore resistance to that
     * damage type, effectively doubling damage against resistant targets. This feat increases average damage
     * slightly, particularly with multi-die spells.
     */
    fun isElvenAccuracy(): Boolean {
        return isRacialTraitEnabled (RacialTrait.ElvenAccuracy)
    }

    fun isElementalAdept(): Boolean {
        return isFeatEnabled(Feat.ElementalAdept)
    }

    fun isGreatWeaponFighting(): Boolean {
        return isFeatEnabled(Feat.GreatWeaponFighting)
    }

    fun getWeaponNames(): List<String> {
        val list = mutableListOf<String>()
        if (characterData.inventory == null) return list
        for (item in characterData.inventory) {
            if (item.definition.filterType == "Weapon") list.add(item.definition.name)
        }
        return list;
    }

    fun getWeaponList(): List<Weapon> {
        val list = mutableListOf<Weapon>()

        if (characterData.inventory == null) return list
        for (item in characterData.inventory) {
            val def = item.definition
            if (def.filterType == "Weapon") {
                var props = mutableListOf<String>()
                if (def.properties != null) {
                    for (prop in def.properties) props.add(prop.name)
                }
                val diceString = def.damage?.diceString ?: "0d4"
                list.add(Weapon (def.name, diceString, props, def.magic, def.attackType ?: 1, def.range ?: 5, def.longRange))
            }
        }
        return list
    }

    fun getRangeAttackModifiers(): Int {
        var mod = 0
        for (modifier in characterData.modifiers.feat) {
            if (modifier.type == "bonus" && modifier.subType == "ranged-weapon-attacks") {
                mod += (modifier.value?: 0)
            }
        }
        return mod
    }

    // this can potentially be used as part both attack and damage bonus calculation
    fun getAbilityWeaponBonus(w: Weapon): Int {
        // first calculate ability bonus, based on weapon type
        val strBonus = Constants.statToBonusMap[getModifiedAbilityScore(AbilityType.Strength)] ?: 0
        val dexBonus = Constants.statToBonusMap[getModifiedAbilityScore(AbilityType.Dexterity)] ?: 0

        val props = w.properties ?: emptyList()
        return if (props.contains("Finesse")) Math.max(strBonus, dexBonus)
        else if (w.attackType == 1) strBonus
        else dexBonus
    }

    fun getAttackBonus(w: Weapon): Int {
        val statBonus = getAbilityWeaponBonus(w)
        val weaponTypeBonus = if (w.attackType == 2) getRangeAttackModifiers() else 0  // TODO: melee attack bonuses ?
        return statBonus + getProficiencyBonus() + weaponTypeBonus // for now assume proficiency in all weapons
    }

    fun getDamageBonus(w: Weapon, isBA: Boolean): Int {
        return if (isBA) 0 else getAbilityWeaponBonus(w) // TODO: two-weapon fighting feat lets you add bonus damage even in BA
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
        println ("is2014        = "+is2014())
        println ("")
        for (item in getWeaponList()) {
            val attackHitBonus      = getAttackBonus(item)
            val attackDamageBonus   = getDamageBonus(item, false)
            //val baDamageBonus       = getDamageBonus(item, true) // for now, this is always 0
            //println("$item, attackHitBonus=$attackHitBonus, attackDamageBonus=$attackDamageBonus, baDamageBonus=$baDamageBonus")
            println("$item, attackHitBonus=$attackHitBonus, attackDamageBonus=$attackDamageBonus")
        }

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
