package com.vikinghelmet.dnd.dprapp.ui.screens

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
import com.vikinghelmet.dnd.dpr.util.DprSettings

@Composable
//@Preview
fun MainScreen(settings: DprSettings,
               onCharacterButtonClicked: () -> Unit,
               onMonsterButtonClicked: () -> Unit,
               onAttackButtonClicked: (Int) -> Unit,
               modifier: Modifier = Modifier
) {
        var proximity   by rememberSaveable { mutableStateOf("") }
        var outputText  by remember { mutableStateOf("") }

        Column(modifier = modifier) {
            Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {

                OutlinedTextField(
                    value = settings.characterName,
                    onValueChange = { },
                    label = { Text("Character") },
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
                    value = settings.monsterName,
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
                            val proximityInt = if (proximity.isEmpty()) 0 else proximity.toInt()
                            onAttackButtonClicked(proximityInt)

                            try {
                                val builder = ScenarioBuilder(character!!, monster!!)
                                val result = builder.runScenarios(proximityInt)
                                outputText = builder.getResultSummary(result)
                            } catch (e: Exception) {
                                println("Unable to build scenarios: $e")
                                outputText = "Invalid value"
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