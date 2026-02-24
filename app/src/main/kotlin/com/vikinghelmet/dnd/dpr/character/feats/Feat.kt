package com.vikinghelmet.dnd.dpr.character.feats

enum class Feat(val id: Int, val traitName: String) {
    // https://www.dndbeyond.com/sources/dnd/phb-2024/feats#GreatWeaponFighting
    GreatWeaponFighting(1789148, "Great Weapon Fighting"),

    // https://www.dndbeyond.com/sources/dnd/phb-2024/feats#ElementalAdept
    ElementalAdept(0,"Elemental Adept"),

    // others found along the way
    GreatWeaponMaster(0, "Great Weapon Master"),
    WeaponMastery(0, "Weapon Mastery"),
    Lucky(0, "Lucky"),
    Telekinetic(0, "Telekinetic"),
    MerchantAbilityScoreImprovements(0, "Merchant Ability Score Improvements"),
}