package com.vikinghelmet.dnd.dprapp.data

import com.vikinghelmet.dnd.dpr.action.Combatant
import com.vikinghelmet.dnd.dpr.editable.EditablePlayerCharacter
import com.vikinghelmet.dnd.dpr.scenario.onesided.ScenarioResult
import com.vikinghelmet.dnd.dpr.util.DprSettings
import com.vikinghelmet.dnd.dpr.util.NumericRange
import kotlinx.serialization.Serializable

@Serializable
data class DprUiState(
    // current character: used when navigating from CombatantScreen to PlanningScreen
    var currentCharacter: EditablePlayerCharacter? = null,

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
            if (combatantA == null) "" else combatantA!!.getName(),
            if (combatantB == null) "" else combatantB!!.getName(),
            proximity
        )
    }
}
