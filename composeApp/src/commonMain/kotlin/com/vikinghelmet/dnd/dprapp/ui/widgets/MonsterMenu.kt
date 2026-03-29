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
import androidx.compose.ui.input.key.utf16CodePoint
import androidx.compose.ui.text.input.ImeAction
import com.vikinghelmet.dnd.dpr.monsters.Monster
import com.vikinghelmet.dnd.dpr.util.Globals
import dev.shivathapaa.logger.api.LogLevel
import dev.shivathapaa.logger.api.LoggerFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonsterMenu(textFieldState: TextFieldState, fillMaxWidth: Boolean, onMenuItemSelected: (Monster?) -> Unit) {
    val logger = LoggerFactory.get("com.vikinghelmet.dnd.dprapp.ui.widgets.MonsterMenu")

    val options = Globals.monsters.map { it.name }

    var expanded by remember { mutableStateOf(false) }
    //val textFieldState = rememberTextFieldState()

    // filter options based on text field value
    val filteringOptions =
        options.filter { it.contains(textFieldState.text, ignoreCase = true) }

    fun handleEnterKey() {
        if (textFieldState.text.toString() == "debug") {
            println("attempting to set debug logging")
            Globals.initLogger(LogLevel.DEBUG)
            logger.info { "debug logging enabled" }
            textFieldState.setTextAndPlaceCursorAtEnd("")
        }
        else {
            val selection = if (filteringOptions.size == 1) filteringOptions[0]
            else filteringOptions.firstOrNull { it.equals(textFieldState.text.toString(), ignoreCase = true) }

            println("handleEnterKey, selection = $selection")

            if (selection != null) {
                onMenuItemSelected(Globals.getMonsterOrNull(selection))
            }
        }
    }

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

                    if (keyEvent.key == Key.Enter || keyEvent.utf16CodePoint == 10) {
                        handleEnterKey()
                        true // Event handled
                    } else {
                        false // Event propagated
                    }
                } ,
            // handle Return key for Mobile
            keyboardOptions  = KeyboardOptions( imeAction = ImeAction.Done ),
            onKeyboardAction = {
                handleEnterKey()
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