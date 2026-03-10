package com.vikinghelmet.dnd.dprapp

// import com.vikinghelmet.dnd.dprlib.CustomFibi

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vikinghelmet.dnd.dpr.CmdTest
import com.vikinghelmet.dnd.dpr.util.Constants
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

fun getCharacter(characterID: String): String {
    var character: com.vikinghelmet.dnd.dpr.character.Character? = null
    runBlocking {
        character = CmdTest.getCharacter(characterID)
    }
    return character!!.toHumanReadableString()
}

@Composable
@Preview
fun App(countries: List<Country> = countries()) {
    MaterialTheme {
        var showCountries by remember { mutableStateOf(false) }
        var timeAtLocation by remember { mutableStateOf("No location selected") }
        var characterId by remember { mutableStateOf("") }
        var monsterName by remember { mutableStateOf("") }

        val foo = Constants.toString() // CustomFibi.toString()
        val buf = StringBuilder()
        //for (i in 1..30) buf.append(". \n") // .append(" ").append(i).append("\n")
        //for (i in 1..30) buf.append(" ").append(i).append("\n")

        //var outputText by remember { mutableStateOf(buf.toString()) }
        var outputText by remember { mutableStateOf(foo) }

        Column(
            modifier = Modifier
                .padding(20.dp)
                .safeContentPadding()
                .fillMaxSize(),
        ) {
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
                    onClick = { outputText += ("\n"+monsterName) }) {
                    Text("M")
                }

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