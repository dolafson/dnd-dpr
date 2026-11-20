package com.vikinghelmet.dnd.dprapp.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.vikinghelmet.dnd.dpr.util.NumericRange
import com.vikinghelmet.dnd.dprapp.data.CoinType
import com.vikinghelmet.dnd.dprapp.ui.widgets.NumericMenu
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
@Composable
fun MoneyScreen(navHostController: NavHostController)
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

    val coinInputMap  = mutableMapOf<CoinType, TextFieldState>()
    CoinType.entries.forEach { it -> coinInputMap[it] = rememberTextFieldState(initialText = "0") }
//    val goldInput   = rememberTextFieldState(initialText = "0")
//    val silverInput = rememberTextFieldState(initialText = "0")
//    val copperInput = rememberTextFieldState(initialText = "0")

    LaunchedEffect(Unit) {
    }

    Column(modifier = Modifier.padding(20.dp).safeContentPadding().fillMaxSize()
                .combinedClickable(onClick = {}, onDoubleClick =  { navHostController.popBackStack() })
    )
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

                CoinType.entries.forEach { coinType ->
                    Box(modifier = outerBoxModifier) {
                        Box(modifier = innerBoxModifier) {
                            BasicTextField(state = coinInputMap.get(coinType)!!, readOnly = false, modifier = inputModifier)
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(start = 20.dp)) {
                Text(" ", modifier = topLabelModifier)

                var remainder = 0
                var remainderEP = 0

                CoinType.entries.forEach { coinType ->
                    val inputText = coinInputMap[coinType]!!.text.toString()
                    println("coinType = ${coinType.name}, inputText = $inputText, playerCount = ${playerCount.current}")

                    Row(modifier = readOnlyRowModifier) {
                        if (playerCount.current <= 0) {
                            Text("0", modifier = readOnlyModifier)
                        }
                        else {
                            val input = inputText.toIntOrNull() ?: 0
                            var output = 0
                            if (coinType == CoinType.Electrum) {
                                output = input / playerCount.current
                                remainderEP = input % playerCount.current
                            }
                            else {
                                val subtotal = remainder * 10 + remainderEP * 5 + input
                                output = subtotal / playerCount.current
                                remainder = subtotal % playerCount.current
                                remainderEP = 0
                            }

                            println("coinType = ${coinType.name}, inputText = $inputText, output = $output, remainder = $remainder")

                            Text("$output", modifier = readOnlyModifier)
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
            Button( onClick = { navHostController.popBackStack() }) { Text("OK") }
        }
    }
}
