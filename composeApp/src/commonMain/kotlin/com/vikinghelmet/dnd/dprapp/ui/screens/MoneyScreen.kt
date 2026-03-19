package com.vikinghelmet.dnd.dprapp.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vikinghelmet.dnd.dpr.util.NumericRange
import com.vikinghelmet.dnd.dprapp.data.CoinType
import com.vikinghelmet.dnd.dprapp.ui.NumericMenu
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
@Composable
fun MoneyScreen(onConfirm: () -> Unit)
{
    val playerCount by remember { mutableStateOf(NumericRange(0,10,0)) }

    val topLabelModifier = Modifier.padding(bottom=10.dp).height(40.dp)
    val labelModifier    = Modifier.padding(bottom=20.dp).height(40.dp)

    val outerPCModifier = Modifier.padding(bottom=10.dp)

    val outerBoxModifier = Modifier.padding(top=10.dp, bottom=10.dp)
    val innerBoxModifier = Modifier.border(2.dp, MaterialTheme.colorScheme.primary)
    val inputModifier    = Modifier.width(50.dp).height(40.dp).padding(start=10.dp, top = 10.dp)

    //val readOnlyModifier = Modifier.width(50.dp).height(30.dp).padding(top = 5.dp, bottom = 5.dp)

    val readOnlyRowModifier = Modifier.padding(bottom=30.dp)
    val readOnlyModifier = Modifier.padding(start=10.dp, bottom = 5.dp)
    val remainderModifier = Modifier.padding(start=20.dp, bottom = 5.dp)

    val goldInput   = rememberTextFieldState(initialText = "0")
    val silverInput = rememberTextFieldState(initialText = "0")
    val copperInput = rememberTextFieldState(initialText = "0")

    LaunchedEffect(Unit) {
    }

    Column(modifier = Modifier.padding(20.dp).safeContentPadding().fillMaxSize())
    {
        Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp))
        {
            Column {
                Text("Players", modifier = topLabelModifier)
                CoinType.entries.forEach { Text(text = it.name, modifier = labelModifier) }
            }

            Column(modifier = Modifier.padding(start = 20.dp)) {
                Box(modifier = outerPCModifier) {
                    NumericMenu(playerCount, {})
                }
                Box(modifier = outerBoxModifier) {
                    Box(modifier = innerBoxModifier) {
                        BasicTextField(state = goldInput, readOnly = false, modifier = inputModifier)
                    }
                }
                Box(modifier = outerBoxModifier) {
                    Box(modifier = innerBoxModifier) {
                        BasicTextField(state = silverInput, readOnly = false, modifier = inputModifier)
                    }
                }
                Box(modifier = outerBoxModifier) {
                    Box(modifier = innerBoxModifier) {
                        BasicTextField(state = copperInput, readOnly = false, modifier = inputModifier)
                    }
                }
            }

            Column(modifier = Modifier.padding(start = 20.dp)) {
                Text(" ", modifier = topLabelModifier)

                var remainder = 0

                CoinType.entries.forEach {
                    val inputText = when (it) {
                        CoinType.Gold -> goldInput.text.toString()
                        CoinType.Silver -> silverInput.text.toString()
                        CoinType.Copper -> copperInput.text.toString()
                    }

                    println("coinType = ${it.name}, inputText = $inputText, playerCount = ${playerCount.current}")

                    Row(modifier = readOnlyRowModifier) {
                        if (playerCount.current <= 0) {
                            Text("0", modifier = readOnlyModifier)
                            /*
                            if (it == CoinType.Copper) {
                                Text("0", modifier = remainderModifier)
                            } */
                        }
                        else {
                            val input = inputText.toIntOrNull() ?: 0
                            val output = (remainder * 10 + input) / playerCount.current
                            remainder = (remainder * 10 + input) % playerCount.current

                            println("coinType = ${it.name}, inputText = $inputText, output = $output, remainder = $remainder")

                            Text("$output", modifier = readOnlyModifier)
/*
                            if (it == CoinType.Copper) {
                                Text("$remainder", modifier = remainderModifier)
                            } */
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button( onClick = { onConfirm() }) { Text("OK") }
        }
    }
}
