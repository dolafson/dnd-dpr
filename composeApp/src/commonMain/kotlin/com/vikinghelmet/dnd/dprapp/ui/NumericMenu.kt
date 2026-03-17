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
import androidx.compose.ui.unit.dp
import com.vikinghelmet.dnd.dpr.util.NumericRangeMap
import com.vikinghelmet.dnd.dprapp.DprViewModel

@Composable
fun NumericMenu(
    statKey: String,
    viewModel: DprViewModel,
    onValueChanged: (Int) -> Unit
) {
    Box(modifier = Modifier
        .padding(horizontal = 0.dp)
        .border(2.dp, MaterialTheme.colorScheme.primary))
    {
        var expanded by remember { mutableStateOf(false) }
        //val options = (min..max).toList()

        //val statSource = viewModel.getStats()
        val statSource = viewModel.uiState.collectAsState().value.statSource
        val rangeMap = statSource?.getNumericRangeMap() ?: NumericRangeMap(false, emptyMap())

        val isEditable = rangeMap.isEditable
        val numericRange = rangeMap.map[statKey]

        var selectedOption by remember { mutableStateOf<Int?>(null) }

        var displayText by remember { mutableStateOf(
            if (selectedOption != null) selectedOption.toString()
                else if (numericRange == null) "?" else numericRange.current.toString())
        }

        /*
        var displayText by remember {
            mutableStateOf(
                if (numericRange == null) "?" else numericRange.current.toString()
            )
        }
*/
       // println("NumericMenu: statKey: $statKey, range = $numericRange")
        /*
        LaunchedEffect(Unit) {
            println("NumericMenu: LaunchedEffect: statKey: $statKey, range = $numericRange")

            displayText = if (numericRange == null) "?" else numericRange.current.toString()
        }
*/
        key(numericRange) {
            displayText =
                if (selectedOption != null) selectedOption.toString()
                else if (numericRange == null) "?" else numericRange.current.toString()
            
            println("inside key, should redraw: statKey: $statKey, range = $numericRange, selectedOption=$selectedOption, displayText: $displayText")

            Text(displayText, modifier = Modifier.clickable { expanded = !expanded }.padding(horizontal = 5.dp))

        }
        //Text("$selectedOption", modifier = Modifier.clickable { expanded = !expanded }.padding(horizontal = 5.dp))

        if (isEditable && numericRange != null) {
            val options = (numericRange.min..numericRange.max).toList()

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.toString()) },
                        onClick = {
                            onValueChanged(option)
                            selectedOption = option
                            displayText = option.toString()
                            expanded = false
                        },
                        modifier = Modifier.width(50.dp)
                    )
                }
            }
        }
    }
}