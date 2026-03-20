package com.vikinghelmet.dnd.dprapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vikinghelmet.dnd.dpr.editable.EditableCharacter
import com.vikinghelmet.dnd.dprapp.DprViewModel
import com.vikinghelmet.dnd.dprapp.data.PlanViewModel
import com.vikinghelmet.dnd.dprapp.ui.widgets.BasicTextMenu
import com.vikinghelmet.dnd.dprapp.ui.widgets.FeatMenu
import com.vikinghelmet.dnd.dprapp.ui.widgets.dprFiles
import kotlin.uuid.ExperimentalUuidApi


@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
@Composable
fun PlanningScreen(viewModel: DprViewModel,
                    onDismiss: () -> Unit,
                    onConfirm: () -> Unit)
{
    var character: EditableCharacter = viewModel.getCurrentCharacter()!!
    var modifyCounter: Int by remember { mutableStateOf(0) }
    var planViewModel = remember { PlanViewModel(character) }

    Column(
        modifier = Modifier
            .padding(20.dp)
            .safeContentPadding()
            .fillMaxSize(),
    ) {
        println("plan = $planViewModel")

        //for (tmpLevel in character.from.getLevel()..20)
        //for (tmpLevel in 1..20)
        for (p in planViewModel.plan)
        {
            if (!p.addFeat && !p.addFS && !p.addSpell) continue

            if (p.level > 1) {
                HorizontalDivider(modifier = Modifier.padding(top = 20.dp), thickness = 2.dp)
            }

            Row(modifier = Modifier.padding(top = 10.dp)) {
                Text("Level ${p.level}", fontWeight = FontWeight.Bold)
            }

            if (p.addFeat) {
                FeatMenu(p, character, false, { feat, asi1, asi2 -> run { p.feat = feat; p.asi1 = asi1; p.asi2 = asi2; }})
            }

            if (p.addFS) {
                FeatMenu(p, character, true) { feat, asi1, asi2 -> run { p.feat = feat; p.asi1 = asi1; p.asi2 = asi2; } }
            }

            if (p.addSpell) {
                for (s in p.spellsToAdd) {
                    Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
                        Column {
                            Text("L${s.spellLevel} Spell", modifier = Modifier.padding(end = 10.dp))
                        }
                        Column (modifier = Modifier.padding(start = 20.dp)) {
                            BasicTextMenu(s.selectedSpell, s.options) {
                                s.selectedSpell = it
                            }
                        }
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
            Button( onClick = {
                //println("after updates, plan = $planViewModel")
                println ("saving plan to local file")
                character.editableFields.plan = planViewModel.toPersistentFormat()
                dprFiles.saveEditableCharacter(character.editableFields)

                onConfirm()
            }) {
                Text("OK")
            }
        }
    }
}
