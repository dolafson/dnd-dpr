package com.vikinghelmet.dnd.dprapp.data

import com.vikinghelmet.dnd.dpr.action.Combatant
import com.vikinghelmet.dnd.dpr.editable.EditablePlayerCharacter
import com.vikinghelmet.dnd.dpr.monsters.Monster
import com.vikinghelmet.dnd.dpr.scenario.onesided.ScenarioResult
import com.vikinghelmet.dnd.dpr.util.DprSettings
import com.vikinghelmet.dnd.dpr.util.NumericRange
import kotlinx.serialization.Serializable

@Serializable
data class DprUiState(
    // current character/monster: whatever is currently displayed on that screen
    var currentCharacter: EditablePlayerCharacter? = null,

    // main character/monster: assigned from current after OK press; used in attack calculation
    var mainCharacter: EditablePlayerCharacter? = null,
    var mainMonster: Monster? = null,

    var combatantA: Combatant? = null,
    var combatantB: Combatant? = null,

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
            if (combatantB == null) "" else combatantB!!.getName(),
            proximity
        )
    }
}
