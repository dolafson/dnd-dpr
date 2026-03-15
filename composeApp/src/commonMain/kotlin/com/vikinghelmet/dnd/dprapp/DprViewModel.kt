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
import com.vikinghelmet.dnd.dpr.util.CharacterListItem
import com.vikinghelmet.dnd.dpr.util.DprSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

private const val PRICE_PER_CUPCAKE = 2.00
private const val PRICE_FOR_SAME_DAY_PICKUP = 3.00

class DprViewModel : ViewModel() {

    /**
     * Cupcake state for this order
     */
    private val _uiState = MutableStateFlow(DprSettings())
    val uiState: StateFlow<DprSettings> = _uiState.asStateFlow()

    /**
     * Set the proximity and TODO: calculate scenarios
     */
    fun setProximity(proximity: Int) {
        _uiState.update { currentState ->
            currentState.copy(
                proximity = proximity,
                // price = calculatePrice(quantity = numberCupcakes)
            )
        }
    }

    fun setCharacterName(characterName: String) {
        _uiState.update { currentState ->
            currentState.copy(characterName = characterName)
        }
    }

    fun setCharacterList(characterList: MutableList<CharacterListItem>) {
        _uiState.update { currentState ->
            currentState.copy(characterList = characterList)
        }
    }

    fun setMonsterName(monsterName: String) {
        _uiState.update { currentState ->
            currentState.copy(monsterName = monsterName)
        }
    }

    fun reset() {
        _uiState.value = DprSettings()
    }

    /*

    private fun calculateScenarios(
        quantity: Int = _uiState.value.quantity,
        pickupDate: String = _uiState.value.date
    ): String {
        var calculatedPrice = quantity * PRICE_PER_CUPCAKE
        // If the user selected the first option (today) for pickup, add the surcharge
        if (pickupOptions()[0] == pickupDate) {
            calculatedPrice += PRICE_FOR_SAME_DAY_PICKUP
        }
        return "$calculatedPrice€"
    }

    @OptIn(ExperimentalTime::class)
    private fun pickupOptions(): List<String> {
        val dateOptions = mutableListOf<String>()
        val now = Clock.System.now()
        val timeZone = TimeZone.currentSystemDefault()
        // add current date and the following 3 dates.
        repeat(4) {
            val day = now.plus(it, DateTimeUnit.DAY, timeZone)
            dateOptions.add(day.toLocalDateTime(timeZone).date.toString())
        }
        return dateOptions
    }
     */
}
