package com.vikinghelmet.dnd.dprapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.vikinghelmet.dnd.dpr.scenario.ScenarioBuilder
import com.vikinghelmet.dnd.dpr.turn.AttackResultFormatter
import com.vikinghelmet.dnd.dpr.util.Constants
import com.vikinghelmet.dnd.dpr.util.Globals
import com.vikinghelmet.dnd.dprapp.*
import com.vikinghelmet.dnd.dprapp.ui.widgets.BasicTextMenu
import com.vikinghelmet.dnd.dprapp.ui.widgets.CharacterMenu
import com.vikinghelmet.dnd.dprapp.ui.widgets.MonsterMenu
import com.vikinghelmet.dnd.dprapp.ui.widgets.NumericMenu
import dev.shivathapaa.logger.api.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
//@Preview
fun MainScreen(viewModel: DprViewModel, navHostController: NavHostController)
{
    var currentProgress by remember { mutableFloatStateOf(0f) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope() // Create a coroutine scope

    val characterTextFieldState = rememberTextFieldState()
    val monsterTextFieldState = rememberTextFieldState()

    //al uriHandler = LocalUriHandler.current
    //val fileOpener = LocalFileOpener.current

    var outputText  by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        Log.w("Main Screen Launched")

        if (viewModel.getMainCharacter() != null) {
            characterTextFieldState.setTextAndPlaceCursorAtEnd(viewModel.getCurrentCharacter()!!.getName())
        }

        if (viewModel.getMainMonster() != null) {
            monsterTextFieldState.setTextAndPlaceCursorAtEnd(viewModel.getMainMonster()!!.name)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp))
    {
        Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {

            Column() {
                CharacterMenu("Select Character", dprFiles.getEditableCharacterList(),
                    characterTextFieldState, true, {},
                    { selectedOption ->
                    viewModel.setMainCharacter (dprFiles.getEditableCharacter(selectedOption))
                    println("from menu selection, set main character = ${ viewModel.getMainCharacter()!!.getName() }")
                })

                Row(modifier = Modifier.padding(top = 10.dp, bottom = 10.dp)) {
                    Text(text = "Level", modifier = Modifier.padding(end = 10.dp))

                    NumericMenu(viewModel.getCharacterLevel(), { newLevel ->
                        viewModel.getMainCharacter()!!.editableFields.level = newLevel
                        outputText = ""
                    })
                }

                MonsterMenu(monsterTextFieldState, false) { selectedMonster ->
                    viewModel.setMainMonster(selectedMonster)
                    monsterTextFieldState.setTextAndPlaceCursorAtEnd(selectedMonster?.name ?: "")
                }

                Row(modifier = Modifier.padding(top = 20.dp, bottom= 10.dp)) {
                    Text(text = "Proximity", modifier = Modifier.padding(end = 10.dp))

                    val options =
                        (viewModel.getMainCharacter()?.getActionsAvailable()?.getRanges() ?: emptyList()).sorted().map {
                            Pair("$it", Color.Black)
                        }

                    BasicTextMenu("${ viewModel.getProximity() }", options, 40.dp, 90.dp) { newValue ->
                        println("new proximity: $newValue")
                        viewModel.setProximity(newValue.toInt())
                    }
//                }

//                Row(modifier = Modifier.padding(top = 10.dp, bottom = 10.dp)) {
                    Text(text = "Turns", modifier = Modifier.padding(start = 20.dp, end = 10.dp))

                    NumericMenu(viewModel.getNumberOfTurns(), { turnCount ->
                        viewModel.getNumberOfTurns().current = turnCount
                        outputText = ""
                    })
                }
            }

            Column() {
                Button(onClick = {
                    println("Main: show view -> character")
                    viewModel.setCurrentCharacter(viewModel.getMainCharacter())
                    navHostController.navigate(ViewType.character.name)
                }) { Text("C") }

                Box(modifier = Modifier.height(60.dp)) {}

                Button(onClick = {
                    println("Main: show view -> monster")
                    navHostController.navigate(ViewType.monster.name)
                }) { Text("M") }

                Button(
                    modifier = Modifier.padding(top = 40.dp),

                    onClick = {
                        if (!viewModel.isReadyForAttack()) {
                            outputText = "select a character and monster before attacking"
                            println(outputText)
                        } else {
                            val proximityInt = viewModel.getProximity()
                            viewModel.setProximity(proximityInt)
                            saveSettings(viewModel)

                            val builder = ScenarioBuilder(viewModel.getMainCharacter()!!, viewModel.getMainMonster()!!)
                            viewModel.setScenarioBuilder(builder)

                            loading = true
                            currentProgress = 5f
//                      scope.launch { loadProgress { progress -> currentProgress = progress }; loading = false }

                            scope.launch {
                                outputText = "Building scenario list ...\n"
                                delay(1)

                                builder.build(proximityInt, viewModel.getNumberOfTurns().current)
                                outputText += "Number of turn options = ${ builder.turnOptions.size }\n"
                                outputText += "Number of scenarios = ${ builder.scenarioList.size }\n"
                                delay(1)

                                while (builder.hasNext()) {
                                    repeat (if (isTinyCpu()) 50 else 1000) { if (builder.hasNext()) {
                                        builder.addNext()
                                        currentProgress = builder.getPercentComplete()
                                    }}
                                    delay(1)
                                    println("currentProgress: $currentProgress")
                                }

                                val scenarioResult = builder.topResults(1).first()

                                val buf = StringBuilder("Highest Avg Damage = ")
                                    .append(Globals.getPercent(scenarioResult.totalDPR)).append("\n").append("\n")

                                for (turn in scenarioResult.scenario.turns) {
                                    buf.append(turn.attacks.map { it.getLabel() }).append("\n")
                                }

                                outputText += buf.toString()

                                loading = false
                            }

                        }
                    }
                ) { Text("A") }
            }
        }

        if (loading && viewModel.getScenarioBuilder()!!.scenarioList.size > 100) {
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

        // export button does not yet work on ios ...
        if (isShareCsvSupported() && viewModel.getScenarioBuilder() != null)
        {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {

                Button(
                    //modifier = Modifier.padding(start = 20.dp, top = 10.dp),
                    onClick = {
                        AttackResultFormatter.isCSV = true

                        val scenarioBuilder = viewModel.getScenarioBuilder()!!
                        val buf = StringBuilder()
                        for (result in scenarioBuilder.topResults(Constants.SCENARIO_OUTPUT_MAX)) {
                            buf.append(result.output()).append("\n")
                        }

                        dprFiles.saveAttackCSV(buf.toString())

                        //val fileURL = dprFiles.getAttackCSVLocalUrl()  // "file:///path/to/your/file.csv"

                        //uriHandler.openUri(fileURL)
                        //fileOpener.openCsvFile(fileURL)
                        //openCsvFile(fileURL.replace("file://", ""))

                        shareCsv("attack.csv", buf.toString())
                    }
                ) { Text("Export") }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Button(onClick = {
                navHostController.navigate(ViewType.money.name)
            } )  { Text("$") }
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

