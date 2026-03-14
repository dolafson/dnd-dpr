package com.vikinghelmet.dnd.dprapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vikinghelmet.dnd.dpr.CmdTest
import com.vikinghelmet.dnd.dpr.character.Character
import com.vikinghelmet.dnd.dprapp.DprUiState
import kotlinx.coroutines.runBlocking

var character: Character? = null

@OptIn(ExperimentalMaterial3Api::class)
@Composable
//@Preview
fun CharacterScreen(dprUiState: DprUiState,
                    onDismiss: () -> Unit,
                    onConfirm: (String) -> Unit)
{
    var characterId by rememberSaveable { mutableStateOf("") }
    var outputText by remember { mutableStateOf("") }

    val options = remember { mutableStateListOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf("") }
    val textFieldState = rememberTextFieldState()

    LaunchedEffect(Unit) {
        options.clear()
        options.addAll (dprFiles.getCharacterList())

        println("character list = "+options)

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

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                TextField(
                    //value = selectedOption,
                    state = textFieldState,
                    label = { Text("DND Beyond URL/ID") },
                    // onValueChange = { it: String -> characterId = it },
                    // onValueChange = { characterId = it },
                    readOnly = false,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded,
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.SecondaryEditable))
                    },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryEditable)
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, color = MaterialTheme.colorScheme.onSurface) },
                            onClick = {
                                selectedOption = option
                                characterId = option
                                textFieldState.setTextAndPlaceCursorAtEnd(characterId)
                                expanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }
        }

        Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
            Button(onClick = {
                // if user selected from menu, load character from local storage
                if (characterId == textFieldState.text.toString() &&
                        dprFiles.getCharacterList().contains(characterId))
                {
                    character = dprFiles.getCharacter(characterId)
                }
                else {
                    // user hand-entered a characterID / URL ... first check for validity
                    val id = CmdTest.getCharacterId(textFieldState.text.toString())
                    if (id != null) {
                        // then update the ID, update menu, and fetch from remote storage
                        // on a good fetch, update local storage as well as the menu
                        characterId = id
                        try {
                            runBlocking {
                                character = CmdTest.getRemoteCharacter(characterId)
                            }
                            if (character != null) {
                                dprFiles.saveCharacter(character!!, characterId)
                                options.add(characterId)
                            }
                        } catch (e: Exception) {
                            //println("Error getting character, $e")
                            outputText = "CharacterID invalid / not found"
                        }
                    }
                }

                if (character != null) {
                    outputText = character!!.toHumanReadableString()
                }

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
