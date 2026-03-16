package com.vikinghelmet.dnd.dprapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vikinghelmet.dnd.dpr.monsters.Monster
import com.vikinghelmet.dnd.dpr.util.DprSettings
import com.vikinghelmet.dnd.dpr.util.Globals

var monster: Monster? = null

fun initMonster(monsterName: String): Boolean {
    try {
        val thisMonster = Globals.getMonster(monsterName)
        monster = thisMonster
        return true
    }
    catch (e: Exception) {
        println("unable to populate monster dialog $e")
    }
    return false
}

@Composable
fun StatHalfBlock(label1: String, label2: String, label3: String,
                  value1: Int?, value2: Int?, value3: Int?,
                  modifier1: Modifier, modifier2: Modifier,
                  editable: Boolean, onValueChanged: (Int) -> Unit)
{
    Column(modifier = modifier1) {
        Text(label1)
        Text(label2)
        Text(label3)
    }
    Column(modifier = modifier2) {
        if (value1 == null) Text("?") else if (!editable) Text(value1.toString()) else NumericMenu(value1,20, value1, onValueChanged)
        if (value2 == null) Text("?") else if (!editable) Text(value2.toString()) else NumericMenu(value2,20, value2, onValueChanged)
        if (value3 == null) Text("?") else if (!editable) Text(value3.toString()) else NumericMenu(value3,20, value3, onValueChanged)
    }
}

@Composable
fun StatBlock(str: Int?, dex: Int?, con: Int?, int: Int?, wis: Int?, cha: Int?, editable: Boolean, onValueChanged: (Int) -> Unit) {
    Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
        StatHalfBlock("STR","DEX","CON",str,dex,con,
            Modifier.padding(start = 0.dp), Modifier.padding(start = 20.dp), editable, onValueChanged)

        StatHalfBlock("INT","WIS","CHA",int,wis,cha,
            Modifier.padding(start = 60.dp), Modifier.padding(start = 20.dp), editable, onValueChanged)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
//@Preview
fun MonsterScreen(settings: DprSettings,
                  onDismiss: () -> Unit,
                  onConfirm: (String) -> Unit)
{
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
        monsterName = settings.monsterName

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
                            }
                        )
                    }
                }
            }
        }

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
                if (initMonster(monsterName)) {
                    selectedMonsterName = monsterName
                }

            }) { Text("View") }
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
                Text((monster?.properties?.dataAcNum ?: "?").toString())
                Text((monster?.properties?.HP ?: "?"))
                Text((monster?.properties?.Speed ?: "?"))
                Text((monster?.properties?.Size ?: "?"))
            }
        }

        HorizontalDivider(modifier = Modifier.padding(top = 20.dp), thickness = 2.dp)//, color = Color.Blue)

        StatBlock(monster?.properties?.STR, monster?.properties?.DEX, monster?.properties?.CON,
            monster?.properties?.INT, monster?.properties?.WIS, monster?.properties?.CHA, false, {})

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
