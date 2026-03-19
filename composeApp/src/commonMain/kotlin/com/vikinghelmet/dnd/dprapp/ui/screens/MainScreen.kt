package com.vikinghelmet.dnd.dprapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.vikinghelmet.dnd.dpr.scenario.ScenarioBuilder
import com.vikinghelmet.dnd.dprapp.DprViewModel

@Composable
//@Preview
fun MainScreen(viewModel: DprViewModel,
               onCharacterButtonClicked: () -> Unit,
               onMonsterButtonClicked: () -> Unit,
               onAttackButtonClicked: (Int) -> Unit,
               onMoneyButtonClicked: () -> Unit,
               modifier: Modifier = Modifier
) {
        val uriHandler = LocalUriHandler.current
        //val fileOpener = LocalFileOpener.current

        var proximity   by rememberSaveable { mutableStateOf("") }
        var outputText  by remember { mutableStateOf("") }
        var scenarioBuilder by remember { mutableStateOf<ScenarioBuilder?>(null) }

        Column(modifier = modifier) {
            Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {

                OutlinedTextField(
                    value = viewModel.getMainCharacter()?.getName() ?: "", // settings.characterName,
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
                    value = viewModel.getMainMonster()?.name ?: "", // settings.monsterName,
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
                        if (!viewModel.isReadyForAttack()) {
                            outputText = "select a character and monster before attacking"
                            println(outputText)
                        } else {
                            val proximityInt = if (proximity.isEmpty()) 0 else proximity.toInt()
                            onAttackButtonClicked(proximityInt)

                            try {
                                val builder = ScenarioBuilder(viewModel.getMainCharacter()!!, viewModel.getMainMonster()!!)
                                val result = builder.runScenarios(proximityInt)
                                outputText = builder.getResultSummary(result)
                                scenarioBuilder = builder
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

    // this export button does not yet work on ios ...
/*
            if (scenarioBuilder != null) {
                Button(
                    //modifier = Modifier.padding(start = 20.dp, top = 10.dp),
                    onClick = {
                        AttackResultFormatter.isCSV = true

                        val buf = StringBuilder()
                        for (result in scenarioBuilder!!.lastResult!!) {
                            buf.append(result.output()).append("\n")
                        }

                        //dprFiles.saveAttackCSV(buf.toString())
                        //val fileURL = dprFiles.getAttackCSVLocalUrl()  // "file:///path/to/your/file.csv"

                        //uriHandler.openUri(fileURL)
                        //fileOpener.openCsvFile(fileURL)
                        //openCsvFile(fileURL.replace("file://", ""))

                        shareCsv("attack.csv", buf.toString())
                    }
                ) { Text("Export") }
            }
*/
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(onClick = onMoneyButtonClicked )  { Text("$") }
            }
        }
}