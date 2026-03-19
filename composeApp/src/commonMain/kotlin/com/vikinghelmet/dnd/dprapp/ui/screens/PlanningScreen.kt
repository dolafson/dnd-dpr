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
import com.vikinghelmet.dnd.dpr.editable.EditableCharacter
import com.vikinghelmet.dnd.dprapp.DprViewModel
import com.vikinghelmet.dnd.dprapp.ui.NumericMenu
import com.vikinghelmet.dnd.dprapp.ui.dprFiles
import kotlin.uuid.ExperimentalUuidApi


@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
@Composable
fun PlanningScreen(viewModel: DprViewModel,
                    onDismiss: () -> Unit,
                    onConfirm: () -> Unit)
{
    var character: EditableCharacter = viewModel.getCurrentCharacter()!!

    var modifyCounter: Int by remember { mutableStateOf(0) }

    var unsavedChanges by remember { mutableStateOf(false) }
    val options = remember { mutableListOf<String>() }
    var expanded by remember { mutableStateOf(false) }
    val textFieldState = rememberTextFieldState()

    val spellSelections = remember(modifyCounter, viewModel.getCharacterLevel()) {
        println("modifyCounter: $modifyCounter")
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
        HorizontalDivider(modifier = Modifier.padding(top = 20.dp), thickness = 2.dp)

        Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
            Column {
                Text("Level")
            }
            Column(modifier = Modifier.padding(start = 20.dp)) {
                NumericMenu(viewModel.getCharacterLevel(), { unsavedChanges = true; modifyCounter ++ })
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onDismiss) { Text("Dismiss") }
            Spacer(Modifier.width(8.dp))
            Button( onClick = { onConfirm() }) { Text("OK") } // TODO: double check this
        }
    }
}
