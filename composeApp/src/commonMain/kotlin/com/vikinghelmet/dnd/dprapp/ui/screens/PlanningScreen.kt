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
import com.vikinghelmet.dnd.dprapp.ui.widgets.BasicTextMenu
import com.vikinghelmet.dnd.dprapp.ui.widgets.FeatMenu
import kotlin.uuid.ExperimentalUuidApi


@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
@Composable
fun PlanningScreen(viewModel: DprViewModel,
                    onDismiss: () -> Unit,
                    onConfirm: () -> Unit)
{
    var character: EditableCharacter = viewModel.getCurrentCharacter()!!

    var modifyCounter: Int by remember { mutableStateOf(0) }
    val textFieldState = rememberTextFieldState()
    val spellsForClass = remember { character.getSpellsForClass() }

    LaunchedEffect(Unit) {
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
        val fsLevelList = character.getLevelsForFightingStyle()

        //for (tmpLevel in character.from.getLevel()..20)
        for (tmpLevel in 1..20)
        {
            val addFeat  = asiLevelList.contains(tmpLevel)
            val addFS    = fsLevelList.contains(tmpLevel)
            val addSpell = character.hasNewSpellSlotsAtCharacterLevel(tmpLevel)
            if (!addFeat && !addFS && !addSpell) continue

            if (tmpLevel > 1) {
                HorizontalDivider(modifier = Modifier.padding(top = 20.dp), thickness = 2.dp)
            }

            Row(modifier = Modifier.padding(top = 10.dp)) {
                Text("Level ${tmpLevel}", fontWeight = FontWeight.Bold)
            }

            if (addFeat) {
                FeatMenu(character)
            }

            if (addFS) {
                FeatMenu(character, true)
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
