package com.vikinghelmet.dnd.dprapp.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun BasicTextMenu(
    optionsWithColor: List<Pair<String, Color>> = mutableListOf(),
    onValueChanged: (String) -> Unit
) {
    Box(modifier = Modifier
        .padding(horizontal = 0.dp)
        .border(2.dp, MaterialTheme.colorScheme.primary))
    {
        var expanded by remember { mutableStateOf(false) }
        var selection by remember { mutableStateOf("") }

        Text(selection, modifier = Modifier.clickable { expanded = !expanded }.padding(horizontal = 5.dp,).width(200.dp))

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.width(200.dp)) {
            optionsWithColor.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.first, color = option.second) },
                    onClick = {
                        onValueChanged(option.first)
                        selection = option.first
                        expanded = false
                    },
                    modifier = Modifier.width(200.dp)
                )
            }
        }
    }
}