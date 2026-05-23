package com.vikinghelmet.dnd.dpr.action

import kotlinx.serialization.Serializable

@Serializable
enum class DamageType {
    // monsters user lower-case, spells use camel-case
    acid, bludgeoning, cold, fire, force, lightning, necrotic, piercing, poison, psychic, radiant, slashing, thunder,
    // add undefined for scenarios where DamageType is not needed
    undefined,
    ;
}