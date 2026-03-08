package com.vikinghelmet.dnd.dpr.character.feats

import com.vikinghelmet.dnd.dpr.character.stats.AbilityType

enum class Feat(val featName: String) {
    // https://www.dndbeyond.com/sources/dnd/phb-2024/feats#GreatWeaponFighting
    GreatWeaponFighting("Great Weapon Fighting"),

    // https://www.dndbeyond.com/sources/dnd/phb-2024/feats#ElementalAdept
    ElementalAdept("Elemental Adept"),

    // Archery("Archery"), // this is only used implicitly, via Character.getRangeAttackModifiers()

    ColdCaster("Cold Caster"),

    /* for future use ?

        GreatWeaponMaster("Great Weapon Master"),
        WeaponMastery("Weapon Mastery"),
        Lucky("Lucky"),
        Telekinetic("Telekinetic"),
        MerchantAbilityScoreImprovements("Merchant Ability Score Improvements"),
     */
    ;

    companion object {
        fun fromShortName(shortName: String): AbilityType? {
            return AbilityType.entries.firstOrNull { it.name.lowercase().startsWith(shortName.lowercase()) } // TODO: featName instead of name ?
        }
    }
}