package com.vikinghelmet.dnd.dprapp

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.vikinghelmet.dnd.dpr.CmdTest
import com.vikinghelmet.dnd.dpr.character.Character
import kotlinx.coroutines.runBlocking

var character: Character? = null

fun getCharacter(characterID: String): String {
    runBlocking {
        character = CmdTest.getCharacter(characterID)
    }

    if (character != null) {
        dprFiles.saveCharacter(character!!, characterID)
    }
    else {
        println("unable to save character, null")
    }

    return character!!.toHumanReadableString()
}

@Composable
//@Preview
fun CharacterDialog(showDialog: Boolean,
                    onDismiss: () -> Unit,
                    onConfirm: (String) -> Unit) {
    var characterId by rememberSaveable { mutableStateOf("") }
    var outputText by remember { mutableStateOf("") }

    if (!showDialog) {
        return
    } else {
        println("settings.characterId = "+settings.characterId)
        characterId = settings.characterId ?: ""
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            //shape = RoundedCornerShape(16.dp),
            //color = Color.White,
            //elevation = 8.dp
        ) {

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

                }

                Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
                    Button(onClick = {outputText = getCharacter(characterId) }) { Text("View") }
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
    }
}
