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
import com.vikinghelmet.dnd.dpr.editable.EditableCharacter
import com.vikinghelmet.dnd.dpr.monsters.Monster
import com.vikinghelmet.dnd.dpr.scenario.ScenarioBuilder
import com.vikinghelmet.dnd.dpr.util.EditableAbilityMap
import com.vikinghelmet.dnd.dpr.util.NumericRange
import com.vikinghelmet.dnd.dprapp.data.DprUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DprViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DprUiState())
    val uiState: StateFlow<DprUiState> = _uiState.asStateFlow()

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

    fun getProximity(): Int = _uiState.value.proximity

    fun isReadyForAttack(): Boolean {
        return _uiState.value.mainCharacter != null && _uiState.value.mainMonster != null
    }

    fun getMainCharacter(): EditableCharacter? { return _uiState.value.mainCharacter }
    fun getMainMonster(): Monster? { return _uiState.value.mainMonster }

    fun getCurrentCharacter(): EditableCharacter? { return _uiState.value.currentCharacter }
    fun getCurrentMonster(): Monster? { return _uiState.value.currentMonster }

    fun getAbilityMap(): EditableAbilityMap {
        return _uiState.value.editableAbilityMap
    }

    fun setAbilityMap(editableAbilityMap: EditableAbilityMap) {
        _uiState.update { currentState ->
            currentState.copy(editableAbilityMap = editableAbilityMap)
        }
    }

    fun getCharacterLevel(): NumericRange {
        return _uiState.value.characterLevel
    }

    fun setCharacterLevel(characterLevel: NumericRange) {
        _uiState.update { currentState ->
            currentState.copy(characterLevel = characterLevel)
        }
    }

    fun setMainCharacter(mainCharacter: EditableCharacter?) {
        _uiState.update { currentState ->
            currentState.copy(mainCharacter = mainCharacter)
        }
    }

    fun setMainMonster(mainMonster: Monster?) {
        _uiState.update { currentState ->
            currentState.copy(mainMonster = mainMonster)
        }
    }


    fun setCurrentCharacter(currentCharacter: EditableCharacter?) {
        _uiState.update { currentState ->
            currentState.copy(currentCharacter = currentCharacter)
        }
        if (currentCharacter != null) {
            setAbilityMap (currentCharacter.getAbilityMap())
            setCharacterLevel (NumericRange(currentCharacter.from.getLevel(),20,currentCharacter.getLevel()))
        }
    }

    fun setCurrentMonster(currentMonster: Monster?) {
        _uiState.update { currentState ->
            currentState.copy(currentMonster = currentMonster)
        }
    }

    fun setScenarioBuilder(scenarioBuilder: ScenarioBuilder) {
        _uiState.update { currentState ->
            currentState.copy(scenarioBuilder = scenarioBuilder)
        }
    }

    fun getScenarioBuilder(): ScenarioBuilder? {
        return _uiState.value.scenarioBuilder
    }

    fun reset() {
        _uiState.value = DprUiState()
    }
}
