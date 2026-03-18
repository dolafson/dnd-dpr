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
fun CharacterScreen(viewModel: DprViewModel,
                    onDismiss: () -> Unit,
                    onConfirm: (EditableCharacter) -> Unit)
{
    var character: EditableCharacter? = viewModel.getCurrentCharacter()

    var unsavedChanges by remember { mutableStateOf(false) }
    val options = remember { mutableListOf<String>() }
    var expanded by remember { mutableStateOf(false) }
    val textFieldState = rememberTextFieldState()

    LaunchedEffect(Unit) {
        options.clear()
        options.addAll (dprFiles.getEditableCharacterList())

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
                                textFieldState.setTextAndPlaceCursorAtEnd(option)
                                viewModel.setCurrentCharacter (dprFiles.getEditableCharacter(option))
                                expanded = false
                                unsavedChanges = false // TODO: prevent menu selection when unsavedChanges = true ?
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }
        }

        Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
            Button(
                enabled = (textFieldState.text.isNotBlank() && isUrlOrID(textFieldState.text.toString())),
                onClick = {
                    val jenny = "8675309"
                    if (textFieldState.text.toString() == jenny) {
                        dprFiles.deleteAll()
                        onDismiss()
                    }
                    else {
                        val getResult: EditableCharacter? = Loader.getEditableCharacter(textFieldState.text.toString())
                        if (getResult != null) {
                            viewModel.setCurrentCharacter(getResult)
                        } else {
                            val addResult = Loader.addEditableCharacter(textFieldState.text.toString())
                            if (addResult != null) {
                                options.add(addResult.getName())
                                viewModel.setCurrentCharacter(addResult)
                                textFieldState.setTextAndPlaceCursorAtEnd(addResult.getName())
                            }
                        }
                    }
                }) { Text("Add") }

            Button(
                modifier = Modifier.padding(start = 20.dp),
                enabled = (textFieldState.text.isNotBlank() && !isUrlOrID(textFieldState.text.toString()) && unsavedChanges),
                onClick = {
                    val name = textFieldState.text.toString()
                    val editableFields = EditableFields.fromScreen(name, character!!,
                    viewModel.getCharacterLevel(), viewModel.getAbilityMap())

                    dprFiles.saveEditableCharacter(editableFields)
                    character = EditableCharacter(character!!, editableFields)
                    viewModel.setMainCharacter(character)

                    if (!options.contains(name)) {
                        options.add(name)
                    }
                }
            ) { Text("Save") }

            Button(
                modifier = Modifier.padding(start = 20.dp),
                enabled = (textFieldState.text.isNotBlank() && options.contains(textFieldState.text.toString())),
                onClick = {
                    val name = textFieldState.text.toString()
                    if (character == viewModel.getMainCharacter()) {
                        viewModel.setMainCharacter(null)
                    }
                    dprFiles.deleteEditableCharacter(name)

                    options.remove(name)
                    viewModel.setCurrentCharacter(null)
                    textFieldState.setTextAndPlaceCursorAtEnd("")
                }
            ) { Text("Del") } // running out of room on ios screen width
        }

        HorizontalDivider(modifier = Modifier.padding(top = 20.dp), thickness = 2.dp)

        // everything below here - except for Dismiss button - requires a valid character
        if (character != null) {
            Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
                Column {
                    Text("Level")
                    Text("Proficiency Bonus")
                    Text("Spell Save DC")
                    Text("Spell Ability")
                    // currently unable to calculate: AC, HP
                }
                Column(modifier = Modifier.padding(start = 20.dp)) {
                    NumericMenu(viewModel.getCharacterLevel(), { unsavedChanges = true })
                    Text(character.getProficiencyBonus().toString())
                    Text(character.getSpellSaveDC().toString())
                    Text(character.getSpellAbilityType())
                }
            }

            HorizontalDivider(modifier = Modifier.padding(top = 20.dp), thickness = 2.dp)

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
                        NumericMenu( viewModel.getAbilityMap().map[it], { unsavedChanges = true })
                    }
                }
                Column(modifier = Modifier.padding(start = 60.dp)) {
                    Text(AbilityType.Intelligence.toShortName())
                    Text(AbilityType.Wisdom.toShortName())
                    Text(AbilityType.Charisma.toShortName())
                }
                Column(modifier = Modifier.padding(start = 20.dp)) {
                    listOf(AbilityType.Intelligence, AbilityType.Wisdom, AbilityType.Charisma).forEach {
                        NumericMenu(viewModel.getAbilityMap().map[it], { unsavedChanges = true })
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(top = 20.dp), thickness = 2.dp)

            if (character.getWeaponList().isNotEmpty())
            {
                Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
                    Column {
                        Text("Weapon", fontWeight = FontWeight.Bold)
                        character.getWeaponList().distinct().forEach { weapon -> Text(weapon.name) }
                    }
                    Column (modifier = Modifier.padding(start = 20.dp)) {
                        Text("Hit", fontWeight = FontWeight.Bold)
                        character.getWeaponList().distinct().forEach { weapon -> Text("+"+character.getAttackBonus(weapon).toString()) }
                    }
                    Column (modifier = Modifier.padding(start = 20.dp)) {
                        Text("Damage", fontWeight = FontWeight.Bold)
                        character.getWeaponList().distinct().forEach { weapon -> Text(
                            if (character.getDamageBonus(weapon, false) == 0) {
                                weapon.damage!!
                            } else {
                                // TODO: BA
                                weapon.damage!! +" + " +character.getDamageBonus(weapon, false).toString()
                            }
                        ) }
                    }
                }
            }

            if (character.getFeatList().isNotEmpty()) {

                HorizontalDivider(modifier = Modifier.padding(top = 20.dp), thickness = 2.dp)

                Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
                    Column {
                        Text("Feat", fontWeight = FontWeight.Bold)
                        character.getFeatList().forEach { feat -> Text(feat.definition.name) }
                    }
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
