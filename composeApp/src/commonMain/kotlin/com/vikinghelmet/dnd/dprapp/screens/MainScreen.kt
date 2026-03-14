package com.vikinghelmet.dnd.dprapp.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vikinghelmet.dnd.dpr.scenario.ScenarioBuilder
import com.vikinghelmet.dnd.dprapp.DprUiState

@Composable
//@Preview
fun MainScreen(dprUiState: DprUiState,
               onCharacterButtonClicked: () -> Unit,
               onMonsterButtonClicked: () -> Unit,
               modifier: Modifier = Modifier
) {
        var proximity   by rememberSaveable { mutableStateOf("") }
        var outputText  by remember { mutableStateOf("") }

        Column(modifier = modifier) {
            Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {

                OutlinedTextField(
                    value = dprUiState.characterId,
                    onValueChange = { },
                    label = { Text("DND Beyond URL/ID") },
                    readOnly = true,
                    enabled = true,
                    singleLine = true
                )

                Button(onClick = {
                    println("Main: show view -> character")
                    onCharacterButtonClicked()
                }) { Text("C") }
            }

            Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
                OutlinedTextField(
                    value = dprUiState.monsterName,
                    onValueChange = { },
                    label = { Text("Monster Name") },
                    readOnly = true,
                    enabled = true,
                    singleLine = true
                )
                Button(onClick = {
                    println("Main: show view -> monster")
                    onMonsterButtonClicked()
                }) { Text("M") }
            }

            Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
                OutlinedTextField(
                    value = proximity,
                    onValueChange = { proximity = it },
                    label = { Text("Target Proximity (feet)") },
                    readOnly = false,
                    enabled = true,
                    singleLine = true
                )
                Button(
                    //modifier = Modifier.padding(start = 20.dp, top = 10.dp),
                    onClick = {
                        if (character == null || monster == null) {
                            outputText = "select a character and monster before attacking"
                            println(outputText)
                        } else {
                            saveSettings(dprUiState.characterId, dprUiState.monsterName, proximity)

                            try {
                                val builder = ScenarioBuilder(character!!, monster!!)
                                val result = builder.runScenarios(proximity.toInt())
                                outputText = builder.getResultSummary(result)
                            } catch (e: Exception) {
                                outputText = "Invalid spell name"
                            }
                        }
                    }
                ) { Text("A") }
            }

            Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {

                OutlinedTextField(
                    value = outputText,
                    onValueChange = { outputText = it },
                    readOnly = true,
                    singleLine = false,
                    //enabled = false,
                    minLines = 10,
                    maxLines = 10,
//                modifier = Modifier.fillMaxWidth().height(100.dp) // Overriding defaults
                    modifier = Modifier.heightIn(100.dp) // Overriding defaults
                )
            }
        }
}