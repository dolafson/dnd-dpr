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
import com.vikinghelmet.dnd.dpr.character.feats.FeatEligibility
import com.vikinghelmet.dnd.dpr.editable.EditableCharacter
import com.vikinghelmet.dnd.dprapp.DprViewModel
import com.vikinghelmet.dnd.dprapp.ui.BasicTextMenu
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

    val spellsForClass = remember { character.getSpellsForClass() }

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
        val asiLevelList = character.getLevelsForAbilityIncrease()

        //for (tmpLevel in character.from.getLevel()..20)
        for (tmpLevel in 1..20)
        {
            val addFeat  = asiLevelList.contains(tmpLevel)
            val addSpell = character.hasNewSpellSlotsAtCharacterLevel(tmpLevel)
            if (!addFeat && !addSpell) continue

            if (tmpLevel > 1) {
                HorizontalDivider(modifier = Modifier.padding(top = 20.dp), thickness = 2.dp)
            }

            Row(modifier = Modifier.padding(top = 10.dp)) {
                Text("Level ${tmpLevel}", fontWeight = FontWeight.Bold)
            }

            if (addFeat) {
                val featList = FeatEligibility.getListByCharacter(character)
                val featNames = featList.map {
                    Pair(it.getNameWithWS(), if (it.fullSupport) Color.Blue else Color.LightGray)
                }

                Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
                    Column {
                        Text("Feat")
                    }
                    Column (modifier = Modifier.padding(start = 20.dp)) {
                        BasicTextMenu(featNames, {})
                    }
                }
            }

            if (addSpell) {
                val slotList = character.getNewSpellSlotsAtCharacterLevel(tmpLevel)

                for (id in slotList.indices) for (i in 1..slotList[id]) {
                    Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
                        val spellLevel = id+1
                        val spellNames = spellsForClass.filter { it.properties.Level == spellLevel }.map { it.name }
                        val spellsWithColor = spellNames.map { it -> Pair(it,Color.Black) }.toList()

                        Column {
                            Text("L${spellLevel} Spell", modifier = Modifier.padding(end = 10.dp))
                        }
                        Column (modifier = Modifier.padding(start = 20.dp)) {
                            BasicTextMenu(spellsWithColor, {})
                        }
                        //TextMenu(spellNames,{} )
                    }
                }
            }
        }
/*
        if (character.getFeatList().isNotEmpty()) {

            HorizontalDivider(modifier = Modifier.padding(top = 20.dp), thickness = 2.dp)

            Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
                Column {
                    Text("Feat", fontWeight = FontWeight.Bold)
                    character.getFeatList().forEach { feat -> Text(feat.definition.name) }
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

 */

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
            modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onDismiss) { Text("Dismiss") }
            Spacer(Modifier.width(8.dp))
            Button( onClick = { onConfirm() }) { Text("OK") } // TODO: double check this
        }
    }
}
