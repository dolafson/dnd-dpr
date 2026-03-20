package com.vikinghelmet.dnd.dprapp.ui.widgets

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
import androidx.compose.ui.unit.dp
import com.vikinghelmet.dnd.dpr.util.NumericRange

@Composable
fun NumericMenu(
    numericRange: NumericRange?,
    onValueChanged: (Int) -> Unit
) {
    Box(modifier = Modifier
        .padding(horizontal = 0.dp)
        .border(2.dp, MaterialTheme.colorScheme.primary))
    {
        if (numericRange == null) {
            Text("?", modifier = Modifier.padding(horizontal = 5.dp))
        }
        else {
            var expanded by remember { mutableStateOf(false) }
            var displayValue by remember(numericRange) { mutableStateOf(numericRange.current) }

            println ("NumericMenu: displayValue = $displayValue, numericRange=$numericRange")


            Text(displayValue.toString(), modifier = Modifier.clickable { expanded = !expanded }.padding(horizontal = 5.dp))

            val options = (numericRange.min..numericRange.max).toList()

            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.toString()) },
                        onClick = {
                            onValueChanged(option)
                            displayValue = option
                            numericRange.current = option
                            expanded = false
                        },
                        modifier = Modifier.width(50.dp)
                    )
                }
            }
        }
    }
}