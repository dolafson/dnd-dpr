package com.vikinghelmet.dnd.dprapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import com.vikinghelmet.dnd.dpr.editable.EditableCharacter
import com.vikinghelmet.dnd.dpr.editable.EditableFields
import com.vikinghelmet.dnd.dprapp.DprViewModel
import com.vikinghelmet.dnd.dprapp.data.Loader
import com.vikinghelmet.dnd.dprapp.ui.widgets.NumericMenu
import com.vikinghelmet.dnd.dprapp.ui.widgets.dprFiles
import kotlin.uuid.ExperimentalUuidApi

fun isUrlOrID(str: String): Boolean {
    return str.startsWith("http://") || str.startsWith("https://") || str.toIntOrNull() != null
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
@Composable
fun CharacterScreen(viewModel: DprViewModel,
                    onDismiss: () -> Unit,
                    onPlan: (EditableCharacter) -> Unit,
                    onConfirm: (EditableCharacter) -> Unit)
{
    var character: EditableCharacter? = viewModel.getCurrentCharacter()

    var modifyCounter: Int by remember { mutableStateOf(0) }

    var unsavedChanges by remember { mutableStateOf(false) }
    val options = remember { mutableListOf<String>() }
    var expanded by remember { mutableStateOf(false) }
    val textFieldState = rememberTextFieldState()

    val spellSelections = remember(modifyCounter, viewModel.getCharacterLevel()) {
        // println("modifyCounter: $modifyCounter")
        character?.getSpellSelectionsBySpellLevel(viewModel.getCharacterLevel().current) ?: emptyMap()
    }

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
                // enabled = (textFieldState.text.isNotBlank() && isUrlOrID(textFieldState.text.toString())),
                enabled = (textFieldState.text.isNotBlank() && !options.contains(textFieldState.text.toString())),
                onClick = {
                    val currentText = textFieldState.text.toString()
                    val jenny = "8675309"
                    if (currentText == jenny) {
                        dprFiles.deleteAll()
                        onDismiss()
                    }
                    else if (options.isNotEmpty() && !isUrlOrID(currentText)) {
                        // old character, new name
                        val editableFields = EditableFields.fromScreen(currentText, character!!,
                            viewModel.getCharacterLevel(), viewModel.getAbilityMap())

                        dprFiles.saveEditableCharacter(editableFields)
                        character = EditableCharacter(character!!, editableFields)
                        viewModel.setMainCharacter(character)

                        if (!options.contains(currentText)) {
                            options.add(currentText)
                        }
                    }
                    else {
                        val getResult: EditableCharacter? = Loader.getEditableCharacter(currentText)
                        if (getResult != null) {
                            viewModel.setCurrentCharacter(getResult)
                        } else {
                            val addResult = Loader.addEditableCharacter(currentText)
                            if (addResult != null) {
                                options.add(addResult.getName())
                                viewModel.setCurrentCharacter(addResult)
                                textFieldState.setTextAndPlaceCursorAtEnd(addResult.getName())
                            }
                        }
                    }
                }) { Text("Add") }
/*
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
*/
            Button(
                modifier = Modifier.padding(start = 20.dp),
                enabled = (textFieldState.text.isNotBlank() && options.contains(textFieldState.text.toString())),
                onClick = {
                    onPlan(character!!)
                }
            ) { Text("Plan") } // running out of room on ios screen width

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
                    NumericMenu(viewModel.getCharacterLevel(), { unsavedChanges = true; modifyCounter ++ })
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

            /*
            if (character.getPreparedSpells().isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(top = 20.dp), thickness = 2.dp)
                Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
                    Column {
                        Text("Prepared Spells", fontWeight = FontWeight.Bold)
                        character.getPreparedSpells().forEach { spell -> Text(spell.name) }
                    }
                }
            }

            if (character.editableFields.plan.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(top = 20.dp), thickness = 2.dp)
                Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
                    Column {
                        Text("PLANNED Spells by Character Level", fontWeight = FontWeight.Bold)
                        for (p in character.editableFields.plan.entries) {
                            //val characterLevel = p.key
                            //val spellsAtLevel = p.value.spells
                            Text("${p.key}: spells = ${p.value.spells}")
                        }
                        character.editableFields.plan.forEach { key ->  }
                    }
                }
            }

             */

            // for (i in character.from.getLevel()..character.getLevel())

            for (selection in spellSelections) {
                val spellLevel = selection.key
                if (selection.value.isNotEmpty()) {
                    HorizontalDivider(modifier = Modifier.padding(top = 20.dp), thickness = 2.dp)
                    Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
                        Column {

                            Text("Level ${spellLevel} Spells", fontWeight = FontWeight.Bold)

                            for (spell in selection.value.readOnlyList) {
                                Text(spell.name)
                            }
                            for (spell in selection.value.editableList) {
                                Text(spell.name, color = Color.Blue)
                            }
                        }
                    }
                }
            }
/*
            for (spellLevel in 1..9) if (
                character.editableFields.plan.isNotEmpty() &&
                character.hasSpellsAtSpellLevel(spellLevel))
            {
                HorizontalDivider(modifier = Modifier.padding(top = 20.dp), thickness = 2.dp)
                Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
                    Column {
                        Text("Level ${spellLevel} Spells", fontWeight = FontWeight.Bold)
                        character.editableFields.plan.forEach { plan ->
                            plan.value.spells.forEach { spellName ->
                                // TODO: optimize this
                                var spell: Spell? = null
                                try {
                                    spell = Globals.getSpell(spellName, character.is2014())
                                }
                                catch (e: Exception) {
                                    println("unable to display details for spell $spellName")
                                }
                                if (spell != null && spell.properties.Level == spellLevel) {
                                    Text(spell.name)
                                }
                            }
                        }

                        //character.getPreparedSpells().forEach { spell -> if (spell.properties.Level == spellLevel) { Text(spell.name) }}
                    }
                }
 */
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
