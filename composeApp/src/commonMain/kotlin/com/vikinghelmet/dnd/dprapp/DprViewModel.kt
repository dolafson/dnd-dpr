/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vikinghelmet.dnd.dprapp

import androidx.lifecycle.ViewModel
import com.vikinghelmet.dnd.dpr.action.Combatant
import com.vikinghelmet.dnd.dpr.action.CombatantMenuItem
import com.vikinghelmet.dnd.dpr.editable.EditablePlayerCharacter
import com.vikinghelmet.dnd.dpr.scenario.combat.Combat
import com.vikinghelmet.dnd.dpr.scenario.onesided.ScenarioResult
import com.vikinghelmet.dnd.dpr.util.NumericRange
import com.vikinghelmet.dnd.dprapp.data.DprUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DprViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DprUiState())
    val uiState: StateFlow<DprUiState> = _uiState.asStateFlow()

    fun getProximity(): Int = _uiState.value.proximity
    fun getNumberOfTurns(): NumericRange = _uiState.value.numberOfTurns

    fun getCurrentCharacter(): EditablePlayerCharacter? = _uiState.value.currentCharacter
    fun getCharacterLevel(): NumericRange = _uiState.value.characterLevel
    fun getScenarioResultList(): List<ScenarioResult>? = _uiState.value.scenarioResultList

    fun getCombatant(onTeamA: Boolean): CombatantMenuItem? {
        return if (onTeamA) _uiState.value.combatantA else _uiState.value.combatantB
    }

    fun getCombatList() = _uiState.value.combatList

    // ------------------------------------------
    fun isReadyForAttack(): Boolean {
        return _uiState.value.combatantA != null && _uiState.value.combatantB != null
    }
    fun isReadyForScenarioBuilder(): Boolean {
        return _uiState.value.combatantA != null && _uiState.value.combatantB != null &&
                _uiState.value.combatantA is Combatant && _uiState.value.combatantB is Combatant
    }

    fun isReadyForExport(): Boolean {
        return (getScenarioResultList() != null && getScenarioResultList()!!.isNotEmpty()) ||
                (getCombatList() != null && getCombatList()!!.isNotEmpty())
    }

    fun setCurrentCharacter(currentCharacter: EditablePlayerCharacter?) {
        _uiState.update { currentState ->
            currentState.copy(currentCharacter = currentCharacter)
        }
    }

    fun setCombatant(combatant: CombatantMenuItem?, onTeamA: Boolean) {
        _uiState.update { currentState ->
            if (onTeamA) {
                currentState.copy(combatantA = combatant)
            }
            else {
                currentState.copy(combatantB = combatant)
            }
        }
    }

    fun setScenarioResultList(scenarioResultList: List<ScenarioResult>) {
        _uiState.update { currentState ->
            currentState.copy(scenarioResultList = scenarioResultList)
        }
    }

    fun setCombatList(combatList: List<Combat>) {
        _uiState.update { currentState ->
            currentState.copy(combatList = combatList)
        }
    }

    /**
     * Set the proximity and TODO: move scenario calculation here?
     */
    fun setProximity(proximity: Int) {
        _uiState.update { currentState ->
            currentState.copy(
                proximity = proximity,
                // price = calculatePrice(quantity = numberCupcakes)
            )
        }
    }

    fun reset() {
        _uiState.value = DprUiState()
    }
}
