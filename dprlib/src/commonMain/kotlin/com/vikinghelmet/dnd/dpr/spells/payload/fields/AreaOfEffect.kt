package com.vikinghelmet.dnd.dpr.spells.payload.fields
import kotlinx.serialization.Serializable

@Serializable
data class AreaOfEffect(var shape: AreaOfEffectShape, var size: String) {

    fun getSizeInFeet(): Int {
        /* aoe.size values, by frequency
87  = "X"
66  = "X foot"
50  = "X foot radius"
10  = "X foot radius, X foot high"
10  = "X feet long, X feet high*"
4  = "X-foot"
4  = "X foot long"
2  = "X foot tall, X foot radius"
2  = "X feet wide, X feet long"
1  = "X-foot-radius"
1  = "X-foot-radius, X-foot-tall"
1  = "X ft"
1  = "X ft radius"
1  = "X foot radius, X foot tall*"
1  = "Ten X foot"
*/
        return size.replace("Ten","10").replace("[ -].*".toRegex(), "").toInt()
    }
}

