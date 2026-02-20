package com.vikinghelmet.dnd.dpr
import kotlinx.serialization.Serializable

@Serializable
data class AreaOfEffect(var shape: AreaOfEffectShape, var size: String)
{
    fun isBig(): Boolean {
        // println("isBig, size = "+size)

        // first field in size is almost always numeric; for now, treat 2-digit size as big
        //return "[0-9][0-9].*".toRegex().matches(size)
        // for now, always true ... ?
        return true
    }
}
