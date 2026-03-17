package com.vikinghelmet.dnd.dprapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vikinghelmet.dnd.dpr.modified.StatBlock
import com.vikinghelmet.dnd.dpr.monsters.Monster
import com.vikinghelmet.dnd.dpr.util.Globals
import com.vikinghelmet.dnd.dprapp.DprViewModel
import com.vikinghelmet.dnd.dprapp.data.Loader
import com.vikinghelmet.dnd.dprapp.ui.StatBlockDisplay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
//@Preview
fun MonsterScreen(viewModel: DprViewModel,
                  onDismiss: () -> Unit,
                  onConfirm: (String) -> Unit)
{
    var monster: Monster? = viewModel.getCurrentMonster()

    val options = Globals.monsters.map { it.name }
    var expanded by remember { mutableStateOf(false) }
    val textFieldState = rememberTextFieldState()

    var monsterName by rememberSaveable { mutableStateOf("") }

    // TODO: figure out why this is needed, and find a better way ...
    // this seems redundant, but we need at least one remember field (other than monsterName above)
    // that is guaranteed to change value whenever the monsterName changes ;
    // without this, NONE of the text fields in the remaining rows will update
    var selectedMonsterName by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.setCurrentMonster(viewModel.getCurrentMonster())

        // monsterName = settings.monsterName
        monster = Loader.getMonster(monsterName)
        monsterName = viewModel.getCurrentMonster()?.name ?: ""

        if (options.contains(monsterName)) {
            selectedMonsterName = monsterName
        }
        if (monsterName.isNotBlank()) {
            textFieldState.setTextAndPlaceCursorAtEnd(monsterName)
        }
    }

    Column(
        modifier = Modifier
            .padding(20.dp)
            .safeContentPadding()
            .fillMaxSize()
    ) {
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            TextField(
                state = textFieldState,
                label = { Text("Monster Name") },
                readOnly = false,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded,
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.SecondaryEditable))
                },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryEditable)
            )

            // filter options based on text field value
            val filteringOptions =
                options.filter { it.contains(textFieldState.text, ignoreCase = true) }

            if (filteringOptions.isNotEmpty()) {
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    filteringOptions.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption, color = MaterialTheme.colorScheme.onSurface) },
                            onClick = {
                                monsterName = selectionOption
                                textFieldState.setTextAndPlaceCursorAtEnd(monsterName)
                                expanded = false

                                println("menu.onClick, monsterName = $monsterName")
                                monster = Loader.getMonster(monsterName)

                                if (monster != null) { // TODO: allow selected = null, to force fields to clear ?
                                    selectedMonsterName = monsterName
                                    viewModel.setCurrentMonster(monster)
                                }
                            }
                        )
                    }
                }
            }
        }
/*
        Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
            Button(onClick = {
                // if these two fields don't match, take the one from the text field (if valid) ...
                // TODO: confirm if this is still needed
                val text = textFieldState.text.toString()
                if (monsterName != text && options.contains(text)) {
                    println("field mismatch, forcing monsterName to text value")
                    monsterName = text
                }

                println("onClick, monsterName = $monsterName")
                monster = Loader.getMonster(monsterName)
                if (monster != null) { // TODO: allow selected = null, to force fields to clear ?
                    selectedMonsterName = monsterName
                    viewModel.setCurrentMonster(monster)
                }

            }) { Text("View") }
        }
*/
        HorizontalDivider(modifier = Modifier.padding(top = 20.dp), thickness = 2.dp)//, color = Color.Blue)

        Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
            Column {
                Text("Monster Name")
                Text("Armor Class")
                Text("Hit Points")
                Text("Speed")
                Text("Size")
            }
            Column(modifier = Modifier.padding(start = 20.dp)) {
                Text(selectedMonsterName)

                if (viewModel.getCurrentMonster() == null) {
                    Text("?")
                    Text("?")
                    Text("?")
                    Text("?")
                }
                else {
                    val m = viewModel.getCurrentMonster()!!
                    //Text((monster?.properties?.dataAcNum ?: "?").toString())
                    Text((m.properties.dataAcNum).toString())
                    Text((m.properties.HP))
                    Text((m.properties.Speed))
                    Text((m.properties.Size))
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(top = 20.dp), thickness = 2.dp)//, color = Color.Blue)

        val stats = StatBlock(monster?.properties?.STR ?: 0, monster?.properties?.DEX?: 0, monster?.properties?.CON?: 0,
            monster?.properties?.INT?: 0, monster?.properties?.WIS?: 0, monster?.properties?.CHA?: 0)

        StatBlockDisplay(viewModel) {}

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 50.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onDismiss) { Text("Dismiss") }
            Spacer(Modifier.width(8.dp))
            Button(onClick = { onConfirm(monsterName ?: "") }) { Text("OK") }
        }
    }
}
