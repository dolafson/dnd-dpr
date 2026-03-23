package com.vikinghelmet.dnd.dprapp.ui.widgets

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.utf16CodePoint
import androidx.compose.ui.text.input.ImeAction
import com.vikinghelmet.dnd.dpr.monsters.Monster
import com.vikinghelmet.dnd.dpr.util.Globals

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonsterMenu(textFieldState: TextFieldState, fillMaxWidth: Boolean, onMenuItemSelected: (Monster?) -> Unit) {
    val options = Globals.monsters.map { it.name }

    var expanded by remember { mutableStateOf(false) }
    //val textFieldState = rememberTextFieldState()

    // filter options based on text field value
    val filteringOptions =
        options.filter { it.contains(textFieldState.text, ignoreCase = true) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        TextField(
            state = textFieldState,
            lineLimits = TextFieldLineLimits.SingleLine,
            label = { Text("Select Monster") },
            readOnly = false,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded,
                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.SecondaryEditable))
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            //modifier = if (!fillMaxWidth) Modifier.menuAnchor(MenuAnchorType.PrimaryEditable)
            //            else              Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryEditable)

            modifier = getModifier(fillMaxWidth).menuAnchor(MenuAnchorType.PrimaryEditable)
                // handle Return key for Desktop
                .onKeyEvent { keyEvent ->
                    expanded = true

                    // for enter key, look for a single selection, or a full-text match (ignoring case)
                    val selection = if (filteringOptions.size == 1) filteringOptions[0]
                        else filteringOptions.firstOrNull { it.equals(textFieldState.text.toString(), ignoreCase = true) }

                    // println ("keyEvent, key  = ${keyEvent.key}")
                    // println ("keyEvent, utf  = ${keyEvent.utf16CodePoint}")
                    // println ("keyEvent, type = ${keyEvent.type}")
                    // println ("keyEvent, native = ${keyEvent.nativeKeyEvent}")

                    if (selection != null && (keyEvent.key == Key.Enter || keyEvent.utf16CodePoint == 10)) {
                        //println ("enterKey: firstOption: $firstOption, currentText: $currentText, filteringOptions: $filteringOptions")
                        onMenuItemSelected(Globals.getMonsterOrNull(selection))
                        true // Event handled
                    } else {
                        false // Event propagated
                    }
                } ,
            // handle Return key for Mobile
            keyboardOptions  = KeyboardOptions( imeAction = ImeAction.Done ),
            onKeyboardAction = {
                if (filteringOptions.size == 1) {
                    onMenuItemSelected(Globals.getMonsterOrNull(filteringOptions[0]))
                }
            }
        )

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

fun getModifier(fillMaxWidth: Boolean): Modifier {
    return if (fillMaxWidth) Modifier.fillMaxWidth() else Modifier
}