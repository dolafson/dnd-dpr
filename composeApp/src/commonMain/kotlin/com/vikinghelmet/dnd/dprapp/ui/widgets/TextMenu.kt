package com.vikinghelmet.dnd.dprapp.ui.widgets

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextMenu(
    options: List<String> = mutableListOf(),
    onValueChanged: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selection by remember { mutableStateOf("") }
    val textFieldState = rememberTextFieldState()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        TextField(
            state = textFieldState,
            label = { Text("Add spell") },
            readOnly = false,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded,
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.SecondaryEditable))
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryEditable)
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
                        onValueChanged(option)
                        selection = option
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}


