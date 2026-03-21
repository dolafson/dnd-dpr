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

        if (viewModel.getCurrentCharacter() != null) {
            println("CharacterScreen: LaunchedEffect: set char name: ${ viewModel.getCurrentCharacter()!!.getName() }")

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
                                println("from menu selection, set current character = ${ viewModel.getCurrentCharacter()!!.getName() }")
                                expanded = false
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
                    if (currentText == "kaboom") {
                        dprFiles.deleteAll()
                        onDismiss()
                    }
                    else if (currentText == "party") {
                        Loader.loadParty().forEach { options.add(it.getName()) }
                        viewModel.setCurrentCharacter(null)
                        textFieldState.setTextAndPlaceCursorAtEnd("")
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


        if (character != null) {
            Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
                Column {
                    Text("Level")
                }
                Column(modifier = Modifier.padding(start = 20.dp)) {
                    NumericMenu(viewModel.getCharacterLevel(), { newLevel ->
                        character.editableFields.level = newLevel
                        modifyCounter ++;
                    })
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(top = 20.dp), thickness = 2.dp)

        // everything below here - except for Dismiss button - requires a valid character ... AND is read-only

        if (character != null) {
            val subclass = character.getSubclassName()

            Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
                Column {
                    Text("Class")
                    if (subclass != null) { Text("Subclass") }

                    Text("Proficiency Bonus")

                    if (character.getSpellAbilityType() != "n/a") {
                        Text("Spell Save DC")
                    }
                    // currently unable to calculate: AC, HP
                }
                Column(modifier = Modifier.padding(start = 20.dp)) {
                    Text(text = character.getClassName())
                    if (subclass != null) { Text(text=subclass) }

                    var current = character.getProficiencyBonus()
                    Text(text = current.toString(), color = highlightIncrease (character.from.getProficiencyBonus(), current))

                    if (character.getSpellAbilityType() != "n/a") {
                        current = character.getSpellSaveDC()
                        Text(current.toString(), color = highlightIncrease (character.from.getSpellSaveDC(), current))
                    }
                }
            }

            Spacer(modifier = Modifier.padding(top = 20.dp))

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
                        val baselineScore = character.from.getModifiedAbilityScore(it)
                        val currentScore = character.getModifiedAbilityScore(it)
                        Text(text = ( currentScore ).toString(), color = highlightIncrease (baselineScore, currentScore))
                    }
                }
                Column(modifier = Modifier.padding(start = 60.dp)) {
                    Text(AbilityType.Intelligence.toShortName())
                    Text(AbilityType.Wisdom.toShortName())
                    Text(AbilityType.Charisma.toShortName())
                }
                Column(modifier = Modifier.padding(start = 20.dp)) {
                    listOf(AbilityType.Intelligence, AbilityType.Wisdom, AbilityType.Charisma).forEach {
                        val baselineScore = character.from.getModifiedAbilityScore(it)
                        val currentScore = character.getModifiedAbilityScore(it)
                        Text(text = ( currentScore ).toString(), color = highlightIncrease (baselineScore, currentScore))
                    }
                }
            }

            if (character.getFeatList().isNotEmpty()) {

                HorizontalDivider(modifier = Modifier.padding(top = 20.dp), thickness = 2.dp)

                Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
                    Column {
                        Text("Feat", fontWeight = FontWeight.Bold)
                        character.getFeatList().forEach { feat -> Text(feat.getNameWithWS()) }
                    }
                }
            }

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

            HorizontalDivider(modifier = Modifier.padding(top = 20.dp), thickness = 2.dp)

            if (character.getWeaponList().isNotEmpty())
            {
                Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
                    Column {
                        Text("Weapon", fontWeight = FontWeight.Bold)
                        character.getWeaponList().distinct().forEach { weapon -> Text(weapon.name.replace(",.*".toRegex(), "") ) }
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

fun highlightIncrease(val1: Int, val2: Int): Color = if (val1 == val2) Color.Black else Color.Blue