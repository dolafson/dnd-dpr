package com.vikinghelmet.dnd.dprapp.data

import com.vikinghelmet.dnd.dpr.action.CombatantMenuItem
import com.vikinghelmet.dnd.dpr.editable.EditablePlayerCharacter
import com.vikinghelmet.dnd.dpr.scenario.combat.Combat
import com.vikinghelmet.dnd.dpr.scenario.onesided.ScenarioResult
import com.vikinghelmet.dnd.dpr.util.DprSettings
import com.vikinghelmet.dnd.dpr.util.NumericRange
import kotlinx.serialization.Serializable

@Serializable
data class DprUiState(
    // current character: used when navigating from CombatantScreen to PlanningScreen
    var currentCharacter: EditablePlayerCharacter? = null,

    var combatantA: CombatantMenuItem? = null,
    var combatantB: CombatantMenuItem? = null,

    // editable field on main screen
    var proximity: Int = 0,

    // editable fields on character screen
    var characterLevel: NumericRange = NumericRange(0,0,0),
    var numberOfTurns: NumericRange = NumericRange(1,5,1),

    var scenarioResultList: List<ScenarioResult>? = null,
    var combatList: List<Combat>? = null,
) {
    fun getSettings(): DprSettings {
        return DprSettings(
            if (combatantA == null) "" else combatantA!!.toString(),
            if (combatantB == null) "" else combatantB!!.toString(),
            proximity
        )
    }
}
