package com.vikinghelmet.dnd.dpr.spells.payload

import com.vikinghelmet.dnd.dpr.spells.payload.fields.AreaOfEffect
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Spell")
data class Spell(
    val name: String,
    val description: String? = null,
    val upcastText: String? = null,
    val aoe: AreaOfEffect? = null, // note: this one is not used, we are using the one in AttackPayload
    val level: Int? = null,
    val school: String? = null,
    val castingTime: String? = null,
    val range: String? = null,
    val duration: String? = null,
    val ritual: Boolean? = null,
    val concentration: Boolean? = null,
    val cantripScale: String? = null,
    val components: Components? = null
) : Payload()
{
    @Serializable
    data class Components(
        val material: Boolean? = null,
        val materialDescription: String? = null,
        val somatic: Boolean? = null,
        val verbal: Boolean? = null
    )
}
