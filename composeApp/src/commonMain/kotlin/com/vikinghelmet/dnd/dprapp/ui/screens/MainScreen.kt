package com.vikinghelmet.dnd.dprapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.vikinghelmet.dnd.dpr.scenario.ScenarioBuilder
import com.vikinghelmet.dnd.dprapp.DprViewModel
import com.vikinghelmet.dnd.dprapp.ui.widgets.BasicTextMenu
import com.vikinghelmet.dnd.dprapp.ui.widgets.NumericMenu
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
//@Preview
fun MainScreen(viewModel: DprViewModel,
               onCharacterButtonClicked: () -> Unit,
               onMonsterButtonClicked: () -> Unit,
               onAttackButtonClicked: (Int) -> Unit,
               onMoneyButtonClicked: () -> Unit,
               modifier: Modifier = Modifier
) {
    var currentProgress by remember { mutableFloatStateOf(0f) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope() // Create a coroutine scope
    val scope2 = rememberCoroutineScope() // Create a coroutine scope

    val uriHandler = LocalUriHandler.current
    //val fileOpener = LocalFileOpener.current

    var proximity   by rememberSaveable { mutableStateOf("") }
    var outputText  by remember { mutableStateOf("") }

    Column(modifier = modifier) {
        Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {

            Column() {
                OutlinedTextField(
                    label = { Text("Character") }, readOnly = true, enabled = true, singleLine = true,
                    value = viewModel.getMainCharacter()?.getName() ?: "",
                    onValueChange = { },
                )

                Row(modifier = Modifier.padding(top = 10.dp, bottom = 10.dp)) {
                    Text(text = "Level", modifier = Modifier.padding(end = 20.dp))

                    NumericMenu(viewModel.getCharacterLevel(), { newLevel ->
                        viewModel.getMainCharacter()!!.editableFields.level = newLevel
                        outputText = ""
                    })
                }

                OutlinedTextField(
                    label = { Text("Monster Name") }, readOnly = true, enabled = true, singleLine = true,
                    value = viewModel.getMainMonster()?.name ?: "",
                    onValueChange = { },
                )

                Row(modifier = Modifier.padding(top = 20.dp, bottom= 10.dp)) {
                    Text(text = "Proximity", modifier = Modifier.padding(end = 20.dp))

                    val options =
                        (viewModel.getMainCharacter()?.getActionsAvailable()?.getRanges() ?: emptyList()).sorted().map {
                            Pair("$it", Color.Black)
                        }

                    BasicTextMenu("${ viewModel.getProximity() }", options, 30.dp, 60.dp) { newValue ->
                        println("new proximity: $newValue")
                        viewModel.setProximity(newValue.toInt())
                    }
                }
            }

            Column() {
                Button(onClick = {
                    println("Main: show view -> character")
                    onCharacterButtonClicked()
                }) { Text("C") }

                Box(modifier = Modifier.height(60.dp)) {}

                Button(onClick = {
                    println("Main: show view -> monster")
                    onMonsterButtonClicked()
                }) { Text("M") }

                Button(
                    modifier = Modifier.padding(top = 20.dp),

                    onClick = {
                        if (!viewModel.isReadyForAttack()) {
                            outputText = "select a character and monster before attacking"
                            println(outputText)
                        } else {
                            val proximityInt =
                                viewModel.getProximity()  // if (proximity.isEmpty()) 0 else proximity.toInt()
                            onAttackButtonClicked(proximityInt)

                            val builder = ScenarioBuilder(viewModel.getMainCharacter()!!, viewModel.getMainMonster()!!)
                            builder.build(proximityInt)
                            viewModel.setScenarioBuilder(builder)

                            loading = true
//                      scope.launch { loadProgress { progress -> currentProgress = progress }; loading = false }

                            scope.launch {
                                while (builder.hasNext()) {
                                    for (i in 1..1000) if (builder.hasNext()) {
                                        builder.addNext()
                                        currentProgress = builder.getPercentComplete()
                                    }
                                    delay(1)
                                    println("currentProgress: $currentProgress")
                                }
                                outputText = builder.getResultSummary()
                                loading = false
                            }

                        }
                    }
                ) { Text("A") }
            }
        }

        if (loading) {
            Spacer(Modifier.height(20.dp))

            LinearProgressIndicator(
                progress = { currentProgress },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(20.dp))
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
        if (viewModel.getScenarioBuilder() != null) {
            /*
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
            */
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Button(onClick = onMoneyButtonClicked )  { Text("$") }
        }
    }
}

/** Iterate the progress value */
suspend fun loadProgress(updateProgress: (Float) -> Unit) {
    for (i in 1..100) {
        updateProgress(i.toFloat() / 100)
        delay(100)
    }
}