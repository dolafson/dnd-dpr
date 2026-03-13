package com.vikinghelmet.dnd.dprapp

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.vikinghelmet.dnd.dpr.monsters.Monster
import com.vikinghelmet.dnd.dpr.util.Globals

var monster: Monster? = null

@Composable
//@Preview
fun MonsterDialog(showDialog: Boolean,
                    onDismiss: () -> Unit,
                    onConfirm: (String) -> Unit) {
    var monsterName by rememberSaveable { mutableStateOf("") }
    var outputText by remember { mutableStateOf("") }

    if (!showDialog) {
        return
    } else {
        println("settings.monsterName = "+settings.monsterName)
        monsterName = settings.monsterName ?: ""
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            //shape = RoundedCornerShape(16.dp),
            //color = Color.White,
            //elevation = 8.dp
        ) {

            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .safeContentPadding()
                    .fillMaxSize(),
            ) {
                Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {

                    OutlinedTextField(
                        value = monsterName?:"",
                        onValueChange = { monsterName = it },
                        label = { Text("Monster Name") },
                        readOnly = false,
                        enabled = true,
                        singleLine = true
                    )

                }

                Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
                    Button(onClick = {
                        try {
                            monster = Globals.getMonster(monsterName?:"")
                            val monsterText = monster.toString() //.description
                            outputText = monsterText
                        }
                        catch (e: Exception) {
                            outputText = "Invalid monster name"
                        }
                    }) { Text("View") }
                }

                Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {

                    OutlinedTextField(
                        value = outputText,
                        onValueChange = { outputText = it },
                        readOnly = true,
                        singleLine = false,
                        //enabled = false,
                        minLines = 10,
                        maxLines = 10,
//                modifier = Modifier.fillMaxWidth().height(100.dp) // Overriding defaults
                        modifier = Modifier.heightIn(100.dp) // Overriding defaults
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Dismiss") }
                    Spacer(Modifier.width(8.dp))
                    Button( onClick = { onConfirm(monsterName ?: "") }) { Text("OK") }
                }
            }
        }
    }
}
