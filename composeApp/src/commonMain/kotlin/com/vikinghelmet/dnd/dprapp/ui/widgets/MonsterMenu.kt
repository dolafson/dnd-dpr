package com.vikinghelmet.dnd.dprapp.ui.widgets

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.vikinghelmet.dnd.dpr.monsters.Monster
import com.vikinghelmet.dnd.dpr.util.Globals

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonsterMenu(textFieldState: TextFieldState, fillMaxWidth: Boolean, onMenuItemSelected: (Monster?) -> Unit) {
    val options = Globals.monsters.map { it.name }

    var expanded by remember { mutableStateOf(false) }
    //val textFieldState = rememberTextFieldState()

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        TextField(
            state = textFieldState,
            label = { Text("Select Monster") },
            readOnly = false,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded,
                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.SecondaryEditable))
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            modifier = if (!fillMaxWidth) Modifier.menuAnchor(MenuAnchorType.PrimaryEditable)
                        else              Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryEditable)
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
                            println("menu.onClick, monsterName = $selectionOption")
                            onMenuItemSelected (Globals.getMonsterOrNull (selectionOption))
                        }
                    )
                }
            }
        }
    }
}