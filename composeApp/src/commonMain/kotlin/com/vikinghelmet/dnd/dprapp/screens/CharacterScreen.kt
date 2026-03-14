package com.vikinghelmet.dnd.dprapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vikinghelmet.dnd.dpr.CmdTest
import com.vikinghelmet.dnd.dpr.character.Character
import com.vikinghelmet.dnd.dprapp.DprUiState
import kotlinx.coroutines.runBlocking

var character: Character? = null

fun getCharacter(characterID: String): String {
    runBlocking {
        character = CmdTest.getCharacter(characterID)
    }

    if (character != null) {
        dprFiles.saveCharacter(character!!, characterID)
        return character!!.toHumanReadableString()
    }
    else {
        println("unable to save character, null")
        return ""
    }
}

@Composable
//@Preview
fun CharacterScreen(dprUiState: DprUiState,
                    onDismiss: () -> Unit,
                    onConfirm: (String) -> Unit) {
    var characterId by rememberSaveable { mutableStateOf("") }
    var outputText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        characterId = dprUiState.characterId
        println("CharacterScreen LaunchedEffect, begin; characterId = " + characterId)
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
                readOnly = false,
                enabled = true,
                singleLine = true,
            )
        }

        Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
            Button(onClick = {
                outputText = getCharacter(characterId)
            }) { Text("View") }
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onDismiss) { Text("Dismiss") }
            Spacer(Modifier.width(8.dp))
            Button( onClick = { onConfirm(characterId) }) { Text("OK") }
        }
    }
}
