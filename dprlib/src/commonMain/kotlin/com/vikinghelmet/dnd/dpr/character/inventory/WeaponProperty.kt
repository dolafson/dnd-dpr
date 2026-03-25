package com.vikinghelmet.dnd.dpr.character.inventory

enum class WeaponProperty {
    Ammunition,
    Finesse,
    Heavy,
    Light,
    Loading,
    Range,
    Reach,
    Thrown,
    TwoHanded,
    Versatile,
;

    fun getNameWithWS(): String {
        return if (this == TwoHanded)  "Two-Handed" else name
    }
}