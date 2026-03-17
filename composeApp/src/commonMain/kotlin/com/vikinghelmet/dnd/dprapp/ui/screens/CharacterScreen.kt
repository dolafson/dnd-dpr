package com.vikinghelmet.dnd.dprapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vikinghelmet.dnd.dpr.character.Character
import com.vikinghelmet.dnd.dpr.modified.CharacterOverrides
import com.vikinghelmet.dnd.dpr.modified.StatBlock
import com.vikinghelmet.dnd.dpr.util.CharacterListItem
import com.vikinghelmet.dnd.dprapp.DprViewModel
import com.vikinghelmet.dnd.dprapp.data.Loader
import com.vikinghelmet.dnd.dprapp.data.Loader.getCharacter
import com.vikinghelmet.dnd.dprapp.ui.NumericMenu
import com.vikinghelmet.dnd.dprapp.ui.StatBlockDisplay
import com.vikinghelmet.dnd.dprapp.ui.dprFiles
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

fun isUrlOrID(str: String): Boolean {
    return str.startsWith("http://") || str.startsWith("https://") || str.toIntOrNull() != null
}

fun addCharacterToList (character: Character, isLocal: Boolean, options: MutableList<CharacterListItem>, viewModel: DprViewModel)
{
    val item = CharacterListItem(character.characterData.id.toString(), character.getName(), isLocal)
    options.add(item)
    viewModel.uiState.value.characterList.add(item)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
@Composable
//@Preview
fun CharacterScreen(viewModel: DprViewModel,
                    onDismiss: () -> Unit,
                    onConfirm: (String) -> Unit,
                    onReset: () -> Unit)
{
    var character: Character? = viewModel.getCurrentCharacter()

    var outputText by remember { mutableStateOf("") }

    var level by remember { mutableStateOf(if (character != null) character!!.getLevel() else 0) }

//    var statBlock by remember { mutableStateOf(if (character != null) character!!.getStatBlock() else
//        StatBlock(0,0,0,0,0,0)) }
    var statBlock= if (character != null) character!!.getStatBlock() else StatBlock(0,0,0,0,0,0)

    var showNameAlert by remember { mutableStateOf(false) }
    var modified by remember { mutableStateOf(false) }

    val options = remember { mutableListOf<CharacterListItem>() }
    var selectedOption = remember { mutableStateOf(CharacterListItem("","",false)) }

    var expanded by remember { mutableStateOf(false) }
    val textFieldState = rememberTextFieldState()

    LaunchedEffect(Unit) {
        options.clear()
        options.addAll (viewModel.uiState.value.characterList)

        println("character list = "+options)
        // println("CharacterScreen LaunchedEffect, begin; characterId = " + characterId)

        if (viewModel.getCurrentCharacter() != null) {
            val name = viewModel.getCurrentCharacter()!!.getName()
            val match = viewModel.uiState.value.getMatchingCharacterItem(name)
            if (match != null) {
                selectedOption.value.copyValues(match)
                textFieldState.setTextAndPlaceCursorAtEnd(selectedOption.value.name)
            }
            println("match = $match")
            println("selectedOption = $selectedOption")
        }
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

                                // outputText = loadCharacter(selectedOption, textFieldState.text.toString(), options, settings, statBlock)
                                viewModel.setCurrentCharacter (Loader.getCharacter (selectedOption))
                                expanded = false

                                // use navigation to reload the entire page ... this will reset the editable boxes ...
                                // this feels like a hack, maybe later we'll find a better way
                                // onReset()
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }
        }

        Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
            Button(
                enabled = (textFieldState.text.isNotBlank() &&
                            isUrlOrID(textFieldState.text.toString()) &&
                            !options.map { op -> op.name }.contains(textFieldState.text.toString())),
                onClick = {
                    // outputText =  loadCharacter(selectedOption, textFieldState.text.toString(), options, settings, statBlock)

                    val getResult: Character? = getCharacter(selectedOption)
                    if (getResult != null) {
                        viewModel.setCurrentCharacter (getResult)
                    }
                    else {
                        val addResult = Loader.addCharacter (selectedOption, textFieldState.text.toString())
                        if (addResult != null) {
                            addCharacterToList(addResult, true, options, viewModel)
                            viewModel.setCurrentCharacter (addResult)
                        }
                    }
                }) { Text("Add") }

            Button(
                modifier = Modifier.padding(start = 20.dp),
                enabled = modified,
                onClick = {
                    println("TODO: Save character, baseline = ${selectedOption.value.name}")

                    for (option in options) {
                        if (option.name == textFieldState.text) {
                            println("warning: name matches an existing character ${option.name}")
                            showNameAlert = true
                        }
                    }
                    if (!showNameAlert) {
                        val characterOverrides = CharacterOverrides(Uuid.random().toString(),
                            character!!.characterData.id!!, level,
                            textFieldState.text.toString(), statBlock)
                        println("overrides = $characterOverrides")
                        dprFiles.saveModifiedCharacter(characterOverrides)

                        addCharacterToList(character!!, false, options, viewModel)
                        viewModel.setMainCharacter(viewModel.getCurrentCharacter()!!)
                    }
                }
            ) { Text("Save") }

            if (showNameAlert) {
                AlertDialog(
                    text = { Text("please choose a new name") },
                    onDismissRequest = {},
                    confirmButton = { TextButton(onClick = { showNameAlert = false }) { Text("OK") } },
                )
            }
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
                if (character == null) {
                    Text("?")
                } else {
                    val min = character!!.getLevel()
                    NumericMenu(min, 20, min, { level = it; modified = true })
/*
                    Row() {
                        Text(min.toString(), modifier = Modifier.padding(end = 20.dp))
                        NumericMenu(0, 20 - min, level, { level = it; modified = true })
                    } */
                }

                Text((character?.getProficiencyBonus() ?: "?" ).toString())
                Text((character?.getSpellSaveDC() ?: "?" ).toString())
                Text((character?.getSpellAbilityType() ?: "?" ))
            }
        }

        HorizontalDivider(modifier = Modifier.padding(top = 20.dp), thickness = 2.dp)//, color = Color.Blue)

        if (character != null) {
            println ("redraw stats, statBlock = $statBlock")
            StatBlockDisplay(statBlock,true, { newValue ->
                modified = true
                println("stat changed: $newValue")
            } )
        }

        HorizontalDivider(modifier = Modifier.padding(top = 20.dp), thickness = 2.dp)//, color = Color.Blue)

        if (character != null && character!!.getWeaponList().isNotEmpty())
        {
            Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
                Column {
                    Text("Weapon", fontWeight = FontWeight.Bold)
                    character!!.getWeaponList().distinct().forEach { weapon -> Text(weapon.name) }
                }
                Column (modifier = Modifier.padding(start = 20.dp)) {
                    Text("Hit", fontWeight = FontWeight.Bold)
                    character!!.getWeaponList().distinct().forEach { weapon -> Text("+"+character!!.getAttackBonus(weapon).toString()) }
                }
                Column (modifier = Modifier.padding(start = 20.dp)) {
                    Text("Damage", fontWeight = FontWeight.Bold)
                    character!!.getWeaponList().distinct().forEach { weapon -> Text(
                        if (character!!.getDamageBonus(weapon, false) == 0) {
                            weapon.damage!!
                        } else {
                            // TODO: BA
                            weapon.damage!! +" + " +character!!.getDamageBonus(weapon, false).toString()
                        }
                    ) }
                }
            }
        }

        if (character != null && character!!.getFeatList().isNotEmpty()) {

            HorizontalDivider(modifier = Modifier.padding(top = 20.dp), thickness = 2.dp)//, color = Color.Blue)

            Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
                Column {
                    Text("Feat", fontWeight = FontWeight.Bold)
                    character!!.getFeatList().forEach { feat -> Text(feat.definition.name) }
                }
            }
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
