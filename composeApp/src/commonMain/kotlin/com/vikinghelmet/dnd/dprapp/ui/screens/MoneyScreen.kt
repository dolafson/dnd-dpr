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
import com.vikinghelmet.dnd.dprapp.DprViewModel
import com.vikinghelmet.dnd.dprapp.data.CoinType
import com.vikinghelmet.dnd.dprapp.ui.NumericMenu
import kotlin.uuid.ExperimentalUuidApi


@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
@Composable
fun MoneyScreen(viewModel: DprViewModel,
                    onDismiss: () -> Unit,
                    onConfirm: () -> Unit)
{
    val playerCount by remember { mutableStateOf(NumericRange(0,10,0)) }
    val width = 50.dp
    val height = 30.dp

    val goldInput = rememberTextFieldState(initialText = "0")
    val silverInput = rememberTextFieldState(initialText = "0")
    val copperInput = rememberTextFieldState(initialText = "0")

    LaunchedEffect(Unit) {
    }

    Column(modifier = Modifier.padding(20.dp).safeContentPadding().fillMaxSize())
    {
        Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp))
        {
            Column(modifier = Modifier.padding(start = 0.dp)) {
                Text("Player Count")
                CoinType.entries.forEach { Text(text = it.name, modifier = Modifier.padding(top=5.dp, bottom=5.dp)) }
            }
            Column(modifier = Modifier.padding(start = 20.dp)) {
                NumericMenu(playerCount, { })

                Box(modifier = Modifier.padding(top=5.dp).border(2.dp, MaterialTheme.colorScheme.primary)) {
                    BasicTextField(state = goldInput, readOnly = false, modifier = Modifier.width(width).height(height).padding(start=10.dp, top = 5.dp))
                }
                Box(modifier = Modifier.padding(top =5.dp).border(2.dp, MaterialTheme.colorScheme.primary)) {
                    BasicTextField(state = silverInput, readOnly = false, modifier = Modifier.width(width).height(height).padding(start =10.dp, top = 5.dp))
                }
                Box(modifier = Modifier.padding(top =5.dp).border(2.dp, MaterialTheme.colorScheme.primary)) {
                    BasicTextField(state = copperInput, readOnly = false, modifier = Modifier.width(width).height(height).padding(start = 10.dp, top = 5.dp))
                }
            }

            Column(modifier = Modifier.padding(start = 20.dp)) {
                Text("")

                var remainder = 0

                CoinType.entries.forEach {
                    val inputText = when (it) {
                        CoinType.Gold -> goldInput.text.toString()
                        CoinType.Silver -> silverInput.text.toString()
                        CoinType.Copper -> copperInput.text.toString()
                    }

                    println("coinType = ${it.name}, inputText = $inputText, playerCount = ${playerCount.current}")

                    Row() {
                        if (playerCount.current <= 0) {
                            Text("0", modifier = Modifier.width(width).height(height).padding(top = 5.dp, bottom = 5.dp))
                            if (it == CoinType.Copper) {
                                Text("0", modifier = Modifier.width(width).height(height).padding(start = 10.dp, top = 5.dp, bottom = 5.dp))
                            }
                        }
                        else {
                            val input = inputText.toIntOrNull() ?: 0
                            val output = (remainder * 10 + input) / playerCount.current
                            remainder = (remainder * 10 + input) % playerCount.current

                            println("coinType = ${it.name}, inputText = $inputText, output = $output, remainder = $remainder")

                            Text(
                                "$output",
                                modifier = Modifier.width(width).height(height).padding(top = 5.dp, bottom = 5.dp)
                            )

                            if (it == CoinType.Copper) {
                                Text("$remainder", modifier = Modifier.width(width).height(height).padding(start = 10.dp, top = 5.dp, bottom = 5.dp))
                            }
                        }
                    }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            /*
            TextButton(onClick = { }) { Text("Calc") }
            Spacer(Modifier.width(8.dp)) */
            Button( onClick = { onConfirm() }) { Text("OK") }
        }
    }
}
