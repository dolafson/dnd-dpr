package com.vikinghelmet.dnd.dprapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import com.vikinghelmet.dnd.dpr.modified.EditableCharacter
import com.vikinghelmet.dnd.dpr.modified.EditableFields
import com.vikinghelmet.dnd.dprapp.DprViewModel
import com.vikinghelmet.dnd.dprapp.data.Loader
import com.vikinghelmet.dnd.dprapp.ui.NumericMenu
import com.vikinghelmet.dnd.dprapp.ui.dprFiles
import kotlin.uuid.ExperimentalUuidApi

fun isUrlOrID(str: String): Boolean {
    return str.startsWith("http://") || str.startsWith("https://") || str.toIntOrNull() != null
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
@Composable
//@Preview
fun CharacterScreen(viewModel: DprViewModel,
                    onDismiss: () -> Unit,
                    onConfirm: (EditableCharacter) -> Unit,
                    onReset: () -> Unit)
{
    var character: EditableCharacter? = viewModel.getCurrentCharacter()

    var level by remember { mutableStateOf(if (character != null) character!!.getLevel() else 0) }

    var showNameAlert by remember { mutableStateOf(false) }
    var modified by remember { mutableStateOf(false) }

    val options = remember { mutableListOf<String>() }
    var expanded by remember { mutableStateOf(false) }
    val textFieldState = rememberTextFieldState()

    LaunchedEffect(Unit) {
        options.clear()
//        options.addAll (viewModel.uiState.value.characterList)
        options.addAll (dprFiles.getEditableCharacterList())

        println("character list = "+options)
        // println("CharacterScreen LaunchedEffect, begin; characterId = " + characterId)

        viewModel.setCurrentCharacter(viewModel.getMainCharacter())

        if (viewModel.getCurrentCharacter() != null) {
            textFieldState.setTextAndPlaceCursorAtEnd(viewModel.getCurrentCharacter()!!.getName())
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
                            text = { Text(option, color = MaterialTheme.colorScheme.onSurface) },
                            onClick = {
                                textFieldState.setTextAndPlaceCursorAtEnd(option)

                                // outputText = loadCharacter(selectedOption, textFieldState.text.toString(), options, settings, statBlock)
                                viewModel.setCurrentCharacter (dprFiles.getEditableCharacter(option))
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
                            !options.contains(textFieldState.text.toString())),
                onClick = {
                    // outputText =  loadCharacter(selectedOption, textFieldState.text.toString(), options, settings, statBlock)

                    val getResult: EditableCharacter? = Loader.getEditableCharacter(textFieldState.text.toString())
                    if (getResult != null) {
                        viewModel.setCurrentCharacter (getResult)
                    }
                    else {
                        val addResult = Loader.addCharacter (textFieldState.text.toString())
                        if (addResult != null) {
                            options.add(addResult.getName())
                            // addCharacterToList(addResult, true, options, viewModel)
                            viewModel.setCurrentCharacter (addResult)
                        }
                    }
                }) { Text("Add") }

            Button(
                modifier = Modifier.padding(start = 20.dp),
                enabled = modified,
                onClick = {
                    for (option in options) {
                        if (option == textFieldState.text) {
                            println("warning: name matches an existing character $option")
                            showNameAlert = true
                        }
                    }
                    if (!showNameAlert) {
                        val editableFields = EditableFields.fromScreen(textFieldState.text.toString(), character!!, viewModel.getNumericRangeMap())

                        println("editableFields = $editableFields")
                        dprFiles.saveEditableCharacter(editableFields)

                        //addCharacterToList(character!!, false, options, viewModel)
                        options.add(character.getName())

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
                NumericMenu(viewModel.getNumericRangeMap().map["level"], { level = it; modified = true })
                Text((character?.getProficiencyBonus() ?: "?" ).toString())
                Text((character?.getSpellSaveDC() ?: "?" ).toString())
                Text((character?.getSpellAbilityType() ?: "?" ))
            }
        }

        HorizontalDivider(modifier = Modifier.padding(top = 20.dp), thickness = 2.dp)//, color = Color.Blue)

        // NOTE: resist the urge to refactor this stat block into common code shared with MonsterScreen
        // that refactoring only leads to misery and woe (mismanaged composable state)
        Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp))
        {
            Column(modifier = Modifier.padding(start = 0.dp)) {
                Text(AbilityType.Strength.toShortName())  // short name for display, full name for stat lookup
                Text(AbilityType.Dexterity.toShortName())
                Text(AbilityType.Constitution.toShortName())
            }
            Column(modifier = Modifier.padding(start = 20.dp)) {
                listOf(AbilityType.Strength, AbilityType.Dexterity, AbilityType.Constitution).forEach {
                    NumericMenu( viewModel.getNumericRangeMap().map[it.name], {})
                }
            }
            Column(modifier = Modifier.padding(start = 60.dp)) {
                Text(AbilityType.Intelligence.toShortName())
                Text(AbilityType.Wisdom.toShortName())
                Text(AbilityType.Charisma.toShortName())
            }
            Column(modifier = Modifier.padding(start = 20.dp)) {
                listOf(AbilityType.Intelligence, AbilityType.Wisdom, AbilityType.Charisma).forEach {
                    NumericMenu(viewModel.getNumericRangeMap().map[it.name], {})
                }
            }
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
            Button( onClick = { onConfirm(character!!) }) { Text("OK") } // TODO: double check this
        }
    }
}
