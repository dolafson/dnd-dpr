package com.vikinghelmet.dnd.dprapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vikinghelmet.dnd.dpr.monsters.Monster
import com.vikinghelmet.dnd.dpr.util.Globals
import com.vikinghelmet.dnd.dprapp.DprViewModel
import com.vikinghelmet.dnd.dprapp.data.Loader

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
        println("main monster: "+viewModel.getMainMonster())

        viewModel.setCurrentMonster(viewModel.getMainMonster())
        println("current monster: "+viewModel.getCurrentMonster())

        monsterName = viewModel.getCurrentMonster()?.name ?: ""
        println("monsterName: $monsterName")

        monster = Loader.getMonster(monsterName)

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

        // NOTE: resist the urge to refactor this stat block into common code shared with CharacterScreen
        // that refactoring only leads to misery and woe (mismanaged composable state)
        Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp))
        {
            Column(modifier = Modifier.padding(start = 0.dp)) {
                Text("STR")
                Text("DEX")
                Text("CON")
            }
            Column(modifier = Modifier.padding(start = 20.dp)) {
                Text(text = (monster?.properties?.STR ?: "?").toString())
                Text(text = (monster?.properties?.DEX ?: "?").toString())
                Text(text = (monster?.properties?.CON ?: "?").toString())
            }
            Column(modifier = Modifier.padding(start = 60.dp)) {
                Text("INT")
                Text("WIS")
                Text("CHA")
            }
            Column(modifier = Modifier.padding(start = 20.dp)) {
                Text(text = (monster?.properties?.INT ?: "?").toString())
                Text(text = (monster?.properties?.WIS ?: "?").toString())
                Text(text = (monster?.properties?.CHA ?: "?").toString())
            }
        }


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
