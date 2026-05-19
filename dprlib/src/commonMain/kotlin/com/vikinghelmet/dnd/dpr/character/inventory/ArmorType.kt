package com.vikinghelmet.dnd.dpr.character.inventory

enum class ArmorType() {
    Unarmored,   // type = 0; 10 + DEX mod
    LightArmor,  // type = 1; ArmorBaseAC + DEX mod (e.g., Leather = 11 + DEX mod)
    MediumArmor, // type = 2; ArmorBaseAC + DEX mod (Max +2 for DEX mod)
    HeavyArmor,  // type = 3; Flat (You do NOT add your Dexterity modifier)
    Shield,      // type = 4; this can be added to armor class, if PC has proficiency
;

}