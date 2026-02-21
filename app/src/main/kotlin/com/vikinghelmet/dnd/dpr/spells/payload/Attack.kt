package com.vikinghelmet.dnd.dpr.spells.payload

import com.vikinghelmet.dnd.dpr.spells.payload.fields.AreaOfEffect
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Attack")
data class Attack(
    val name: String? = null,
    val description: String? = null,
    val save: Save? = null,
    val aoe: AreaOfEffect? = null,    //  "aoe":{"shape":"Sphere","size":.....
    val attack: Attack? = null,
    val actionType: String? = null,
    val autoHit: Boolean? = null,
    val onHitDisplay: String? = null,
    val range: String? = null,
    val repeat: Int? = null
)   : Payload()
{
    @Serializable
    data class Save (
        val saveAbility: String,        // "Wisdom",
        val onFail: String? = null,     // "onFail": "Take 5d8 Force damage.",
        val onSucceed: String? = null,  // "onSucceed": "Half as much damage."
    )
    @Serializable
    data class Attack(
        val proficiencyLevel: String? = null,
        val type: String
    )
}