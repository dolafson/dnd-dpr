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

@Composable
fun NumericMenu(
    statKey: String,
    rangeMap: NumericRangeMap,
    onValueChanged: (Int) -> Unit
) {
    Box(modifier = Modifier
        .padding(horizontal = 0.dp)
        .border(2.dp, MaterialTheme.colorScheme.primary))
    {
        val rememberedRangeMap = rangeMap // by remember { mutableStateOf(rangeMap) }

        if (!rememberedRangeMap.isEditable ||
            rememberedRangeMap.map.isEmpty() ||
            !rememberedRangeMap.map.containsKey(statKey))
        {
            Text("?", modifier = Modifier.padding(horizontal = 5.dp))
        }
        else {
            var expanded by remember { mutableStateOf(false) }

            // initialize selected to null when we get a new range map ...
            var selectedOption by remember(rememberedRangeMap) { mutableStateOf<Int?>(null) }

            var displayText by remember(selectedOption,rememberedRangeMap) { mutableStateOf(
                if (selectedOption != null) selectedOption.toString()
                    else rememberedRangeMap.map[statKey]!!.current.toString())
            }

            println ("NumericMenu: statKey=$statKey, displayText = $displayText, selectedOption=$selectedOption, rangeMap=$rangeMap")

            Text(displayText, modifier = Modifier.clickable { expanded = !expanded }.padding(horizontal = 5.dp))

            val options = (rememberedRangeMap.map[statKey]!!.min..rememberedRangeMap.map[statKey]!!.max).toList()

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