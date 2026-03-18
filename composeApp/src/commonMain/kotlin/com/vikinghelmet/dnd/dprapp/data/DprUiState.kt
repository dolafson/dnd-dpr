package com.vikinghelmet.dnd.dprapp.data

import com.vikinghelmet.dnd.dpr.modified.EditableCharacter
import com.vikinghelmet.dnd.dpr.monsters.Monster
import com.vikinghelmet.dnd.dpr.util.DprSettings
import com.vikinghelmet.dnd.dpr.util.NumericRangeMap
import kotlinx.serialization.Serializable

@Serializable
data class DprUiState(
    // read-only fields on main screen
    var mainCharacter: EditableCharacter? = null,
    var mainMonster: Monster? = null,

    // read-write field on main screen
    var proximity: Int = 0,

    // read-write fields on other screens
    var currentCharacter: EditableCharacter? = null,
    var currentMonster: Monster? = null,

    // the source varies with the screen currently viewed - character or monster
    //var statSource: HasNumericRangeMap? = null,
    var numericRangeMap: NumericRangeMap = NumericRangeMap(emptyMap()),
) {
    fun getSettings(): DprSettings {
        return DprSettings(
            if (mainCharacter == null) "" else mainCharacter!!.getName(),
            if (mainMonster == null) "" else mainMonster!!.name,
            proximity
        )
    }
}
