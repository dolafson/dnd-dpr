package com.vikinghelmet.dnd.dprapp.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NumericInput(
    input: Int,
    onValueChanged: (Int) -> Unit
) {
    Box(modifier = Modifier
        .padding(horizontal = 0.dp)
        .border(2.dp, MaterialTheme.colorScheme.primary))
    {
        var displayValue by remember(input) { mutableStateOf(input) }

        println ("NumericInput: displayValue = $displayValue, input=$input")

/*
        BasicTextField(value = displayValue.toString(), modifier = Modifier.padding(horizontal = 5.dp), onValueChange = onValueChanged) {})
        Text(text = displayValue.toString(), modifier = Modifier.padding(horizontal = 5.dp), onValueChanged = {

        }) */
    }
}