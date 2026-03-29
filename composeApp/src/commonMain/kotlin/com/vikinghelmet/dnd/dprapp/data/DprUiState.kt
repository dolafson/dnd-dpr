package com.vikinghelmet.dnd.dprapp.data

import com.vikinghelmet.dnd.dpr.editable.EditableCharacter
import com.vikinghelmet.dnd.dpr.monsters.Monster
import com.vikinghelmet.dnd.dpr.scenario.ScenarioResult
import com.vikinghelmet.dnd.dpr.util.DprSettings
import com.vikinghelmet.dnd.dpr.util.NumericRange
import kotlinx.serialization.Serializable

@Serializable
data class DprUiState(
    // current character/monster: whatever is currently displayed on that screen
    var currentCharacter: EditableCharacter? = null,
    var currentMonster: Monster? = null,

    // main character/monster: assigned from current after OK press; used in attack calculation
    var mainCharacter: EditableCharacter? = null,
    var mainMonster: Monster? = null,

    // editable field on main screen
    var proximity: Int = 0,

    // editable fields on character screen
    var characterLevel: NumericRange = NumericRange(0,0,0),
    var numberOfTurns: NumericRange = NumericRange(1,5,1),

    var scenarioResultList: List<ScenarioResult>? = null,
) {
    fun getSettings(): DprSettings {
        return DprSettings(
            if (mainCharacter == null) "" else mainCharacter!!.getName(),
            if (mainMonster == null) "" else mainMonster!!.name,
            proximity
        )
    }
}
