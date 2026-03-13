package com.vikinghelmet.dnd.dprapp

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.vikinghelmet.dnd.dpr.monsters.Monster
import com.vikinghelmet.dnd.dpr.util.Globals

var monster: Monster? = null

fun populateFields(formFields: MutableMap<String, String>) {
    formFields["AC"] = (monster?.properties?.dataAcNum ?: "?").toString()
}

@Composable
//@Preview
fun MonsterDialog(showDialog: Boolean,
                    onDismiss: () -> Unit,
                    onConfirm: (String) -> Unit) {
    var monsterName by rememberSaveable { mutableStateOf("") }
    var outputText by remember { mutableStateOf("") }
    //var formFields by remember { mutableStateMapOf<String, String>() }
    var formFields by remember { mutableStateOf(mutableMapOf<String, String>()) }
    var ac by remember { mutableStateOf("") }

    if (!showDialog) {
        return
    }
    // reset field whenever dialog opens
    LaunchedEffect(Unit) {
        println("settings.monsterName = "+settings.monsterName)
        monsterName = settings.monsterName ?: ""

        monster = Globals.getMonster(monsterName?:"")
        val monsterText = monster.toString() //.description
        outputText = monsterText
        //formFields["AC"] = (monster?.properties?.dataAcNum ?: "?").toString()

        populateFields(formFields)
        // formFields["AC"] = (monster?.properties?.dataAcNum ?: "?").toString()
        ac = (monster?.properties?.dataAcNum ?: "?").toString()

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
                            println("new monsterName = $monsterName")
                            monster = Globals.getMonster(monsterName?:"")
                            println("new monster = "+ (monster?.name ?: ""))
                            println("new AC = "+ (monster?.properties?.dataAcNum ?: "?"))

                            val monsterText = monster.toString() //.description
                            outputText = monsterText
                            populateFields(formFields)
                            // formFields["AC"] = (monster?.properties?.dataAcNum ?: "?").toString()
                            ac = (monster?.properties?.dataAcNum ?: "?").toString()
                        }
                        catch (e: Exception) {
                            outputText = "Invalid monster name"
                        }
                    }) { Text("View") }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(top = 20.dp),
                    thickness = 2.dp, // Sets the height of the line
                    color = Color.Blue // Sets the color of the line
                )

                Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
                    Text("Armor Class")
//                    Text((monster?.properties?.dataAcNum ?: "?").toString(),
//                        modifier = Modifier.padding(start = 20.dp))
                    Text(formFields["AC"] ?: "", modifier = Modifier.padding(start = 20.dp))
                    Text(ac, modifier = Modifier.padding(start = 20.dp))
                }

                Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
                    Text("Hit Points")
                    Text((monster?.properties?.HP ?: "?"), modifier = Modifier.padding(start = 20.dp))
                }

                Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
                    Text("Speed")
                    Text((monster?.properties?.Speed ?: "?"), modifier = Modifier.padding(start = 20.dp))
                }

                Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
                    Text("Size")
                    Text((monster?.properties?.Size ?: "?"), modifier = Modifier.padding(start = 20.dp))
                }

                HorizontalDivider(
                    modifier = Modifier.padding(top = 20.dp),
                    thickness = 2.dp, // Sets the height of the line
                    color = Color.Blue // Sets the color of the line
                )

                Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
                    Text("STR")
                    Text((monster?.properties?.STR ?: "?").toString(), modifier = Modifier.padding(start = 20.dp))

                    Text("INT", modifier = Modifier.padding(start = 60.dp))
                    Text((monster?.properties?.INT ?: "?").toString(), modifier = Modifier.padding(start = 20.dp))
                }

                Row(modifier = Modifier.padding(start = 20.dp)) {
                    Text("DEX")
                    Text((monster?.properties?.DEX ?: "?").toString(), modifier = Modifier.padding(start = 20.dp))

                    Text("WIS", modifier = Modifier.padding(start = 60.dp))
                    Text((monster?.properties?.WIS ?: "?").toString(), modifier = Modifier.padding(start = 20.dp))
                }

                Row(modifier = Modifier.padding(start = 20.dp)) {
                    Text("CON")
                    Text((monster?.properties?.CON ?: "?").toString(), modifier = Modifier.padding(start = 20.dp))

                    Text("CHA", modifier = Modifier.padding(start = 60.dp))
                    Text((monster?.properties?.CHA ?: "?").toString(), modifier = Modifier.padding(start = 20.dp))
                }
/*
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
*/

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 50.dp)
                            ,
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
