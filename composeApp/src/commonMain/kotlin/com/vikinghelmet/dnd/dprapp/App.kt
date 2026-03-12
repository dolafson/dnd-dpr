package com.vikinghelmet.dnd.dprapp

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vikinghelmet.dnd.dpr.CmdTest
import com.vikinghelmet.dnd.dpr.character.Character
import com.vikinghelmet.dnd.dpr.monsters.Monster
import com.vikinghelmet.dnd.dpr.scenario.ScenarioBuilder
import com.vikinghelmet.dnd.dpr.util.Globals
import dpr.composeapp.generated.resources.Res
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

data class Country(val name: String, val zone: TimeZone)

fun currentTimeAt(location: String, zone: TimeZone): String {
    fun LocalTime.formatted() = "$hour:$minute:$second"

    val time = Clock.System.now()
    val localTime = time.toLocalDateTime(zone).time

    return "The time in $location is ${localTime.formatted()}"
}

fun countries() = listOf(
    Country("Japan", TimeZone.of("Asia/Tokyo")),
    Country("France", TimeZone.of("Europe/Paris")),
    Country("Mexico", TimeZone.of("America/Mexico_City")),
    Country("Indonesia", TimeZone.of("Asia/Jakarta")),
    Country("Egypt", TimeZone.of("Africa/Cairo")),
)

var character: Character? = null
var monster: Monster? = null

fun getCharacter(characterID: String): String {
    runBlocking {
        character = CmdTest.getCharacter(characterID)
    }

    if (character != null) {
        CmdTest.writeToFile(character!!.getJson(), "example.txt")
    }

    return character!!.toHumanReadableString()
}

@Composable
@Preview
fun App(countries: List<Country> = countries()) {
    MaterialTheme {

        var characterId by rememberSaveable { mutableStateOf("") }
        var monsterName by rememberSaveable { mutableStateOf("") }
        var spellName   by rememberSaveable { mutableStateOf("") }
        var proximity   by rememberSaveable { mutableStateOf("") }

        var outputText by remember { mutableStateOf("") }

        LaunchedEffect(Unit) {
            for (filename in mutableListOf("files/spells.json","files/extra.spells.json")) {
                Globals.addSpells(Res.readBytes(filename).decodeToString())
            }
            Globals.addMonsters(Res.readBytes("files/monsters.json").decodeToString())
        }

        Column(
            modifier = Modifier
                .padding(20.dp)
                .safeContentPadding()
                .fillMaxSize(),
        ) {
            /*
            Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
                OutlinedTextField(
                    value = monsterString, onValueChange = {},
                    label = { Text(monsterString) }, readOnly = true, singleLine = false,
                    maxLines = 5
                    )
            }*/
            Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {

                OutlinedTextField(
                    value = characterId,
                    onValueChange = { characterId = it },
                    label = { Text("DND Beyond URL/ID") },
                    readOnly = false,
                    enabled = true,
                    singleLine = true
                )

                Button(
                    //modifier = Modifier.padding(start = 20.dp, top = 10.dp),
                    onClick = { outputText = getCharacter(characterId) }) {
                    Text("C")
                }
            }

            Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
                OutlinedTextField(
                    value = monsterName,
                    onValueChange = { monsterName = it },
                    label = { Text("Monster Name") },
                    readOnly = false,
                    enabled = true,
                    singleLine = true
                )
                Button(
                    //modifier = Modifier.padding(start = 20.dp, top = 10.dp),
                    onClick = {
                        try {
                            monster = Globals.getMonster(monsterName)
                            val monsterText = monster.toString() //.description
                            outputText = monsterText
                        }
                        catch (e: Exception) {
                            outputText = "Invalid monster name"
                        }
                    }
                ) { Text("M") }
            }
/*********************
            Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
                OutlinedTextField(
                    value = spellName,
                    onValueChange = { spellName = it },
                    label = { Text("Spell Name") },
                    readOnly = false,
                    enabled = true,
                    singleLine = true
                )
                Button(
                    //modifier = Modifier.padding(start = 20.dp, top = 10.dp),
                    onClick = {
                        if (character == null) {
                            outputText = "select a character before a spell"
                        }
                        else {
                            try {
                                val spellText = Globals.getSpell(spellName, character!!.is2014()).description
                                outputText = spellText
                            }
                            catch (e: Exception) {
                                outputText = "Invalid spell name"
                            }
                        }
                    }
                ) { Text("S") }
            }
 *********************/

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
                        }
                        else {
                            try {
                                val builder = ScenarioBuilder(character!!,monster!!)
                                val result = builder.runScenarios (proximity.toInt())
                                outputText = builder.getResultSummary(result)
                            }
                            catch (e: Exception) {
                                outputText = "Invalid spell name"
                            }
                        }
                    }
                ) { Text("A") }
            }


            /*
            Text(
                timeAtLocation,
                style = TextStyle(fontSize = 20.sp),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally)
            )
            Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
                DropdownMenu(
                    expanded = showCountries,
                    onDismissRequest = { showCountries = false }
                ) {
                    countries().forEach { (name, zone) ->
                        DropdownMenuItem(
                            text = { Text(name) },
                            onClick = {
                                timeAtLocation = currentTimeAt(name, zone)
                                showCountries = false
                            }
                        )
                    }
                }
            }
            Button(
                modifier = Modifier.padding(start = 20.dp, top = 10.dp),
                onClick = { showCountries = !showCountries }) {
                Text("Select Location")
            }
*/

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
}