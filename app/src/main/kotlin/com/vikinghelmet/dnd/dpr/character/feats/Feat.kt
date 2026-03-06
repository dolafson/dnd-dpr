package com.vikinghelmet.dnd.dpr.character.feats

enum class Feat(val traitName: String) {
    // https://www.dndbeyond.com/sources/dnd/phb-2024/feats#GreatWeaponFighting
    GreatWeaponFighting("Great Weapon Fighting"),

    // https://www.dndbeyond.com/sources/dnd/phb-2024/feats#ElementalAdept
    ElementalAdept("Elemental Adept"),

    Archery("Archery"),

    // others found along the way
    GreatWeaponMaster("Great Weapon Master"),
    WeaponMastery("Weapon Mastery"),
    Lucky("Lucky"),
    Telekinetic("Telekinetic"),
    MerchantAbilityScoreImprovements("Merchant Ability Score Improvements"),

    ColdCaster("Cold Caster")
}