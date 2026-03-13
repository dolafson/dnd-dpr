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
import com.vikinghelmet.dnd.dpr.DprFiles
import com.vikinghelmet.dnd.dpr.scenario.ScenarioBuilder
import com.vikinghelmet.dnd.dpr.util.Globals
import com.vikinghelmet.dnd.dpr.util.Settings
import dpr.composeapp.generated.resources.Res

val dprFiles = DprFiles(getDocumentsDirPath())
val settings = Settings()

fun saveSettings(characterId: String, monsterName: String, proximity: String) {
    settings.characterId = characterId
    settings.monsterName = monsterName
    settings.proximity = proximity.toInt()
    DprFiles(getDocumentsDirPath()).saveSettings(settings)
}

@Composable
@Preview
fun App() {
    MaterialTheme {
        var showCharacterDialog by remember { mutableStateOf(false) }
        var showMonsterDialog   by remember { mutableStateOf(false) }

        var monsterName by rememberSaveable { mutableStateOf("") }
        var characterId by rememberSaveable { mutableStateOf("") }
        var proximity   by rememberSaveable { mutableStateOf("") }

        var outputText  by remember { mutableStateOf("") }

        LaunchedEffect(Unit) {
            for (filename in mutableListOf("files/spells.json","files/extra.spells.json")) {
                Globals.addSpells(Res.readBytes(filename).decodeToString())
            }
            Globals.addMonsters(Res.readBytes("files/monsters.json").decodeToString())

            dprFiles.init()
            settings.copy (other = dprFiles.getSettings())

            characterId = settings.characterId ?: ""
            monsterName = settings.monsterName ?: ""
            proximity = settings.proximity?.toString() ?: ""
        }

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
                    readOnly = true,
                    enabled = true,
                    singleLine = true
                )

                Button(onClick = { showCharacterDialog = true } ) {  Text("C") }

                CharacterDialog(showCharacterDialog,
                    { showCharacterDialog = false },
                    { dialogSelectedValue ->
                                     println("OK button clicked!")
                                     characterId = dialogSelectedValue
                                     showCharacterDialog = false
                                     outputText = getCharacter(characterId)
                                     saveSettings(characterId, monsterName?: "", proximity)
                                }
                )
            }

            Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
                OutlinedTextField(
                    value = monsterName?: "",
                    onValueChange = { monsterName = it },
                    label = { Text("Monster Name") },
                    readOnly = true,
                    enabled = true,
                    singleLine = true
                )
                Button(onClick = { showMonsterDialog = true } ) { Text("M") }

                MonsterDialog(showMonsterDialog,
                    { showMonsterDialog = false },
                    { dialogSelectedValue ->
                        println("OK button clicked!")
                        monsterName = dialogSelectedValue
                        showMonsterDialog = false
                        saveSettings(characterId, monsterName, proximity)
                    }
                )
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
                        }
                        else {
                            saveSettings(characterId, monsterName, proximity)

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