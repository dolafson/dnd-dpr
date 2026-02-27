package com.vikinghelmet.dnd.dpr.spells.payload.fields
import kotlinx.serialization.Serializable

@Serializable
data class AreaOfEffect(var shape: AreaOfEffectShape, var size: String)
