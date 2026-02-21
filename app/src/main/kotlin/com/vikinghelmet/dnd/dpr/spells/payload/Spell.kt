package com.vikinghelmet.dnd.dpr.spells.payload

import com.vikinghelmet.dnd.dpr.spells.payload.fields.AreaOfEffect
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Spell")
data class Spell(
    val name: String,
    val description: String,
    val upcastText: String? = null,
    val aoe: AreaOfEffect? = null,
    val level: Int,
    val school: String,
    val castingTime: String,
    val range: String,
    val duration: String,
    val ritual: Boolean? = null,
    val concentration: Boolean? = null,
    val cantripScale: String? = null,
    val components: Components
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
