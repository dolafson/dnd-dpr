package com.vikinghelmet.dnd.dprapp.ui.widgets

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.input.ImeAction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterMenu(label: String, options: List<String>, textFieldState: TextFieldState,
                  isReadOnly: Boolean, onAddText: (String) -> Unit, onMenuItemSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    //val textFieldState = rememberTextFieldState()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        TextField(
            state = textFieldState,
            lineLimits = TextFieldLineLimits.SingleLine,
            label = { Text(label) },
            readOnly = isReadOnly,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded,
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.SecondaryEditable))
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),

            modifier = if (isReadOnly)  Modifier.menuAnchor(MenuAnchorType.PrimaryEditable)
            else            Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryEditable)
                // handle Return key for Desktop
                .onKeyEvent { keyEvent ->
                    if (keyEvent.key == Key.Enter) {
                        onAddText(textFieldState.text.toString())
                        true // Event handled
                    } else {
                        false // Event propagated
                    }
                } ,
            // handle Return key for Mobile
            keyboardOptions = if (!isReadOnly) KeyboardOptions( imeAction = ImeAction.Done, ) else KeyboardOptions.Default,
            onKeyboardAction = { if (!isReadOnly) onAddText(textFieldState.text.toString()) }
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
                        expanded = false
                        onMenuItemSelected(option)
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}
