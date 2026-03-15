package com.vikinghelmet.dnd.dprapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vikinghelmet.dnd.dpr.CmdTest
import com.vikinghelmet.dnd.dpr.character.Character
import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import com.vikinghelmet.dnd.dpr.util.CharacterListItem
import com.vikinghelmet.dnd.dpr.util.DprSettings
import kotlinx.coroutines.runBlocking
import kotlin.time.Clock

var character: Character? = null

fun initCharacter(settings: DprSettings) {
    val match = getMatchingCharacterItem(settings)
    if (match != null) {
        character = dprFiles.getCharacter(match.remoteId)
    }
}

fun getMatchingCharacterItem(settings: DprSettings): CharacterListItem? {
    for (item in settings.characterList) {
        if (item.name == settings.characterName) {
            return item
        }
    }
    return null
}

fun loadCharacter(selectedOption: MutableState<CharacterListItem>, text: String, options: MutableList<CharacterListItem>, settings: DprSettings): String {
    // if user selected from menu, load character from local storage
    var remoteId: String? = selectedOption.value.remoteId
    if (remoteId!!.isNotBlank() && dprFiles.getCharacterList().contains(remoteId))
    {
        character = dprFiles.getCharacter(remoteId)
        if (character != null) {
            settings.characterName = selectedOption.value.name
        }
    }
    else {
        val urlOrId = text
        // user hand-entered a characterID / URL ... first check for validity
        remoteId = CmdTest.getCharacterId(urlOrId)
        if (remoteId != null) {
            // then update the ID, update menu, and fetch from remote storage
            // on a good fetch, update local storage as well as the menu
            try {
                runBlocking {
                    character = if (urlOrId.contains("http") && !urlOrId.contains("dndbeyond")) {
                        // content hosted somewhere other than dndbeyond
                        CmdTest.getRemoteCharacterByUrl(urlOrId)
                    }
                    else {
                        // default: dndbeyond
                        CmdTest.getRemoteCharacter(remoteId!!)
                    }
                }
                if (character != null) {
                    remoteId = character!!.characterData.id.toString()
                    dprFiles.saveCharacter(character!!, remoteId)

                    val name = character!!.characterData.name // TODO: ensure uniqueness ...
                    val epochSeconds: Long = Clock.System.now().epochSeconds
                    val item = CharacterListItem(remoteId, remoteId+":"+epochSeconds, name)

                    options.add(item)
                    settings.characterList.add(item)
                    settings.characterName = name
                }
            } catch (e: Exception) {
                //println("Error getting character, $e")
                return "CharacterID invalid / not found"
            }
        }
    }

    if (character != null) {
        return character!!.toStringExtra()
    }
    return ""
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
//@Preview
fun CharacterScreen(settings: DprSettings,
                    onDismiss: () -> Unit,
                    onConfirm: (String) -> Unit)
{
    var outputText by remember { mutableStateOf("") }

    val options = remember { mutableListOf<CharacterListItem>() }
    var selectedOption = remember { mutableStateOf(CharacterListItem("","","")) }

    var expanded by remember { mutableStateOf(false) }
    val textFieldState = rememberTextFieldState()

    LaunchedEffect(Unit) {
        options.clear()
        options.addAll (settings.characterList)

        println("character list = "+options)
        // println("CharacterScreen LaunchedEffect, begin; characterId = " + characterId)
        val match = getMatchingCharacterItem(settings)
        if (match != null) {
            selectedOption.value.copyValues(match)
            textFieldState.setTextAndPlaceCursorAtEnd(selectedOption.value.name)
        }
        println("match = $match")
        println("selectedOption = $selectedOption")
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
                    state = textFieldState,
                    label = { Text("Select/Add Character") },
                    //value = {
                    //    Text(if (selectedOption.value.name.isNotBlank()) selectedOption.value.name else "Select/Add Character")
                    //},
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
                    //println("ExposedDropdownMenu: options $options")

                    options.forEach { option ->
                        //println("DropdownMenuItem: option $option")

                        DropdownMenuItem(
                            text = { Text(option.name, color = MaterialTheme.colorScheme.onSurface) },
                            onClick = {
                                selectedOption.value.copyValues(option)
                                textFieldState.setTextAndPlaceCursorAtEnd(option.name)
                                loadCharacter(selectedOption, textFieldState.text.toString(), options, settings)
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
                outputText = loadCharacter(selectedOption, textFieldState.text.toString(), options, settings)
            }) { Text("View") }
        }

        HorizontalDivider(modifier = Modifier.padding(top = 20.dp), thickness = 2.dp)//, color = Color.Blue)

        // ID/Name are arguably redundant ...
        // FieldValue("ID", (character?.characterData?.id ?: "?" ).toString())
        // FieldValue("Name", character?.characterData?.name ?: "?" )

        Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
            Column {
                Text("Level")
                Text("Proficiency Bonus")
                Text("Spell Save DC")
                Text("Spell Ability")
                // currently unable to calculate: AC, HP
            }
            Column(modifier = Modifier.padding(start = 20.dp)) {
                Text((character?.getLevel() ?: "?" ).toString())
                Text((character?.getProficiencyBonus() ?: "?" ).toString())
                Text((character?.getSpellSaveDC() ?: "?" ).toString())
                Text((character?.getSpellAbilityType() ?: "?" ))
            }
        }

        HorizontalDivider(modifier = Modifier.padding(top = 20.dp), thickness = 2.dp)//, color = Color.Blue)

        StatBlock(
            character?.getModifiedAbilityScore(AbilityType.Strength),
            character?.getModifiedAbilityScore(AbilityType.Dexterity),
            character?.getModifiedAbilityScore(AbilityType.Constitution),
            character?.getModifiedAbilityScore(AbilityType.Intelligence),
            character?.getModifiedAbilityScore(AbilityType.Wisdom),
            character?.getModifiedAbilityScore(AbilityType.Charisma)
        )

        HorizontalDivider(modifier = Modifier.padding(top = 20.dp), thickness = 2.dp)//, color = Color.Blue)

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
            Button( onClick = { onConfirm(selectedOption.value.name) }) { Text("OK") }
        }
    }
}
