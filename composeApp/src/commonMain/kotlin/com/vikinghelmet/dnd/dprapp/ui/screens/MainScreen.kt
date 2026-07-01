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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.vikinghelmet.dnd.dpr.action.Combatant
import com.vikinghelmet.dnd.dpr.action.CombatantMenuItem
import com.vikinghelmet.dnd.dpr.action.results.AttackResultFormatter
import com.vikinghelmet.dnd.dpr.monsters.Monster
import com.vikinghelmet.dnd.dpr.scenario.combat.Combat
import com.vikinghelmet.dnd.dpr.scenario.combat.CombatLoop
import com.vikinghelmet.dnd.dpr.scenario.onesided.ScenarioBuilder
import com.vikinghelmet.dnd.dpr.scenario.onesided.ScenarioCalculator
import com.vikinghelmet.dnd.dpr.scenario.onesided.ScenarioIterator
import com.vikinghelmet.dnd.dpr.scenario.onesided.ScenarioResult
import com.vikinghelmet.dnd.dpr.util.*
import com.vikinghelmet.dnd.dprapp.*
import com.vikinghelmet.dnd.dprapp.ui.widgets.BasicTextMenu
import com.vikinghelmet.dnd.dprapp.ui.widgets.CombatantMenu
import com.vikinghelmet.dnd.dprapp.ui.widgets.NumericMenu
import dev.shivathapaa.logger.api.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun getCombatants() = dprFiles.getPartyList() + dprFiles.getEditableCharacterList() + Globals.monsters

@Composable
//@Preview
fun MainScreen(viewModel: DprViewModel, navHostController: NavHostController)
{
    var numTargets by remember { mutableStateOf(1) }
    var targetSpacing by remember { mutableStateOf(5) }

    var currentProgress by remember { mutableFloatStateOf(0f) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope() // Create a coroutine scope

    val teamATextFieldState = rememberTextFieldState()
    val teamBTextFieldState = rememberTextFieldState()

    val uriHandler = LocalUriHandler.current
    //val fileOpener = LocalFileOpener.current

    var outputText  by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        Log.w("Main Screen Launched")

        if (viewModel.getCombatant(true) != null) {
            //teamATextFieldState.setTextAndPlaceCursorAtEnd(viewModel.getCurrentCharacter()!!.getName())
            teamATextFieldState.setTextAndPlaceCursorAtEnd(viewModel.getCombatant(true)!!.toString())
        }

        if (viewModel.getCombatant(false) != null) {
            teamBTextFieldState.setTextAndPlaceCursorAtEnd(viewModel.getCombatant(false)!!.toString())
        }
    }

    fun runScenarioBuilder() {
        viewModel.setCombatList(emptyList())

        val proximityInt = viewModel.getProximity()
        viewModel.setProximity(proximityInt)
        saveSettings(viewModel)

        val teamA = viewModel.getCombatant(true)!! as Combatant
        val teamB = viewModel.getCombatant(false)!! as Combatant

        val builder = ScenarioBuilder(teamA, teamB)

        scope.launch {
            outputText = "Building scenario list ...\n"
            delay(1)

            val scenarioList = builder.build(proximityInt, viewModel.getNumberOfTurns().current, numTargets, targetSpacing)

            outputText += "Number of turn options = ${ builder.turnOptions.size }\n"
            outputText += "Number of scenarios = ${ scenarioList.size }\n"
            delay(1)

            // avoid showing the progress bar for small data sets (avoid UI flicker)
            if (scenarioList.size > 100) {
                loading = true
                currentProgress = 5f
            }

            val resultList: MutableList<ScenarioResult> = mutableListOf()

            val iterator = ScenarioIterator(scenarioList)
            while (iterator.hasNext()) {
                repeat (if (isTinyCpu()) 50 else 1000) { if (iterator.hasNext()) {
                    val scenario = iterator.next()
                    resultList.add (ScenarioCalculator(scenario).calculateDPRForAllTurns())
                    currentProgress = iterator.getPercentComplete()
                }}
                delay(1)
                println("currentProgress: $currentProgress")
            }

            viewModel.setScenarioResultList(resultList)

            val topResultList = ScenarioResult.topResults(resultList, 1)
            if (topResultList.isNotEmpty()) {
                val topResult = topResultList[0]

                val buf = StringBuilder("Highest Avg Damage = ")
                    .append(Globals.getPercent(topResult.totalDamage)).append("\n").append("\n")

                for (turn in topResult.scenario.turns) {
                    buf.append(turn.attacks.map { it.getLabel() }).append("\n")
                }

                outputText += buf.toString()
            }
            loading = false
        }
    }

    fun pickTeam(selected: CombatantMenuItem): List<Combatant> {
        return when (selected) {
            is Party    -> selected.characterList
            is Monster  -> List(numTargets) { selected }
            else        -> listOf(selected as Combatant)
        }
    }

    fun runCombat() {
        viewModel.setScenarioResultList(emptyList())

        val teamA = pickTeam (viewModel.getCombatant(true)!!)
        val teamB = pickTeam (viewModel.getCombatant(false)!!)

        val numSimulations = 30 // TODO ?
        val loop = CombatLoop(teamA, teamB, numSimulations, false)
        loop.log()

        val combatList = mutableListOf<Combat>()

        scope.launch {
            outputText = "Starting combat loop\n"
            delay(1)
            loading = true

            repeat(numSimulations) {
                combatList.add (loop.runOnce())
                currentProgress = loop.getPercentComplete()
                //outputText += "."
                delay(1)
                println("currentProgress: $currentProgress")
            }

            outputText += "\n\nTeamA win percentage = ${ Globals.getPercent(loop.getTeamAWinPercentage()) }%"
        }
        loading = false
        viewModel.setCombatList(combatList)
    }

    fun getScenarioResultCSV(): String {
        AttackResultFormatter.isCSV = true
        val fileContent = StringBuilder()

        ScenarioResult.topResults (viewModel.getScenarioResultList()!!, Constants.SCENARIO_OUTPUT_MAX)
            .forEach {
                fileContent.append(it.output()).append("\n")
            }

        return fileContent.toString()
    }

    fun getCombatResultCSV(): String {
        val fileContent = StringBuilder()
        viewModel.getCombatList()!!.forEach { fileContent.append(it.output()).append("\n")  }
        return fileContent.toString()
    }

    fun exportCSV(csvUploadUrl: String?) {
        val result = if (viewModel.getCombatList() != null && viewModel.getCombatList()!!.isNotEmpty()) {
            getCombatResultCSV()
        }
        else {
            getScenarioResultCSV()
        }

        if (isShareCsvSupported()) {
            shareCsv("attack.csv", result)
        }
        else {
            var csvDownloadUrl: String? = null

            if (csvUploadUrl != null) runBlocking {
                println("csvUploadUrl = $csvUploadUrl")
                csvDownloadUrl = CharacterAPI.postRequest(csvUploadUrl, result)
                println("csvDownloadUrl = $csvDownloadUrl")
            }

            if (csvDownloadUrl != null) {
                uriHandler.openUri(csvDownloadUrl.trim())
            }
        }
    }


    Column(modifier = Modifier.fillMaxSize().padding(16.dp))
    {
        Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {

            Column() {
                CombatantMenu(teamATextFieldState, false, getCombatants()) { selectedCombatant ->
                    viewModel.setCombatant(selectedCombatant, true) // TODO: fix hard-coding ?
                    teamATextFieldState.setTextAndPlaceCursorAtEnd(selectedCombatant?.toString() ?: "")
                }

                Row(modifier = Modifier.padding(top = 20.dp, bottom = 10.dp)) {
                }

                CombatantMenu(teamBTextFieldState, false, getCombatants()) { selectedCombatant ->
                    viewModel.setCombatant(selectedCombatant, false) // TODO: fix hard-coding ?
                    teamBTextFieldState.setTextAndPlaceCursorAtEnd(selectedCombatant?.toString() ?: "")
                }

                Row(modifier = Modifier.padding(top = 20.dp, bottom = 10.dp)) {
                    Text(text = "Proximity", modifier = Modifier.padding(end = 10.dp))

                    val teamA = viewModel.getCombatant(true)
                    val ranges = if (teamA is Combatant) teamA.getActionsAvailable().getRanges() else emptyList()
                    val options = ranges.sorted().map { Pair("$it", Color.Black) }

                    BasicTextMenu("${viewModel.getProximity()}", options, 40.dp, 90.dp) { newValue ->
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

                Row(modifier = Modifier.padding(bottom = 10.dp)) {
                    Text(text = "Spacing", modifier = Modifier.padding(end = 20.dp))
/*
                    NumericMenu(NumericRange(5, 20, 5), { newNumTargets ->
                        numTargets = newNumTargets
                    }) */

                    val spacingOptions = listOf("5","10","15","20").map { Pair(it, Color.Black )}
                    BasicTextMenu("${targetSpacing}", spacingOptions, 40.dp, 90.dp) { newValue ->
                        targetSpacing = newValue.toInt()
                        println("new radius: $targetSpacing")
                    }

                    Text(text = "Targets", modifier = Modifier.padding(start = 20.dp, end = 10.dp))

                    NumericMenu(NumericRange(1, 10, numTargets), { newNumTargets ->
                        numTargets = newNumTargets
                        println("new numTargets: $numTargets")
                    })
                }
            }

            Column() {
                Button(onClick = {
                    println("Main: show view -> character")
                    navHostController.navigate(ViewType.teamA.name)
                }) { Text("A") }

                Box(modifier = Modifier.height(60.dp)) {}

                Button(onClick = {
                    println("Main: show view -> monster")
                    navHostController.navigate(ViewType.teamB.name)
                }) { Text("B") }

                Button(
                    enabled = viewModel.isReadyForScenarioBuilder(),
                    modifier = Modifier.padding(top = 40.dp),
                    onClick = { runScenarioBuilder() }
                ) { Text("X") }

                Button(
                    enabled = viewModel.isReadyForAttack(),
                    modifier = Modifier.padding(top = 5.dp),
                    onClick = { runCombat() }
                ) { Text("Y") }
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

        // export button behavior may vary on mobile vs desktop
        val csvUploadUrl = Secrets.getCsvUploadUrl()

        if (viewModel.isReadyForExport() && (isShareCsvSupported() || csvUploadUrl != null))
        {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button( onClick = { exportCSV(csvUploadUrl) } ) {
                    Text("Export")
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Button(onClick = { navHostController.navigate(ViewType.money.name) } )  {
                Text("$")
            }
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
