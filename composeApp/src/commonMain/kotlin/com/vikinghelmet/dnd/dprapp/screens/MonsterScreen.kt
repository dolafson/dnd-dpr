package com.vikinghelmet.dnd.dprapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vikinghelmet.dnd.dpr.monsters.Monster
import com.vikinghelmet.dnd.dpr.util.Globals
import com.vikinghelmet.dnd.dprapp.DprUiState

var monster: Monster? = null

@Composable
fun FieldValue(fieldName: String, value: String) {
    //println("FieldValue, formFields: $formFields")
    Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
        Text(fieldName)
        Text(value ?: "x", modifier = Modifier.padding(start = 20.dp))
    }
}

@Composable
fun DoubleWideRow(label1: String, value1: String, label2: String, value2: String) {
    Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
        Text(label1)
        Text(value1, modifier = Modifier.padding(start = 20.dp))

        Text(label2, modifier = Modifier.padding(start = 60.dp))
        Text(value2, modifier = Modifier.padding(start = 20.dp))
    }
}
@Composable
//@Preview
fun MonsterScreen(dprUiState: DprUiState,
                  onDismiss: () -> Unit,
                  onConfirm: (String) -> Unit)
{
    var monsterName by rememberSaveable { mutableStateOf("") }

    // TODO: figure out why this is needed, and find a better way ...
    // this seems redundant, but we need at least one remember field (other than monsterName above)
    // that is guaranteed to change value whenever the monsterName changes ;
    // without this, NONE of the text fields in the remaining rows will update
    var selectedMonsterName by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        monsterName = dprUiState.monsterName
        println("MonsterScreen LaunchedEffect, begin; monsterName = " + monsterName)

        try {
            monster = Globals.getMonster(monsterName ?: "")
            selectedMonsterName = monsterName
        }
        catch (e: Exception) {
            println("unable to populate monster dialog $e")
        }
    }

    Column(
        modifier = Modifier
            .padding(20.dp)
            .safeContentPadding()
            .fillMaxSize()
    ) {
        Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
            OutlinedTextField(
                value = monsterName ?: "",
                onValueChange = { monsterName = it },
                label = { Text("Monster Name") },
                readOnly = false,
                enabled = true,
                singleLine = true
            )
        }

        Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
            Button(onClick = {
                println("onClick, monsterName = $monsterName")
                try {
                    monster = Globals.getMonster(monsterName ?: "")
                    selectedMonsterName = monsterName
                } catch (e: Exception) {
                    println("Invalid monster name")
                }
            }) { Text("View") }
        }

        HorizontalDivider(modifier = Modifier.padding(top = 20.dp), thickness = 2.dp, color = Color.Blue)

        FieldValue("Monster Name", selectedMonsterName)
        FieldValue("Armor Class", (monster?.properties?.dataAcNum ?: "?").toString())
        FieldValue("Hit Points", (monster?.properties?.HP ?: "?"))
        FieldValue("Speed", (monster?.properties?.Speed ?: "?"))
        FieldValue("Size", (monster?.properties?.Size ?: "?"))

        HorizontalDivider(modifier = Modifier.padding(top = 20.dp), thickness = 2.dp, color = Color.Blue)

        DoubleWideRow(
            "STR",(monster?.properties?.STR ?: "?").toString(),
            "INT", (monster?.properties?.INT ?: "?").toString())

        DoubleWideRow(
            "DEX",(monster?.properties?.DEX ?: "?").toString(),
            "WIS", (monster?.properties?.WIS ?: "?").toString())

        DoubleWideRow(
            "CON",(monster?.properties?.CON ?: "?").toString(),
            "CHA", (monster?.properties?.CHA ?: "?").toString())

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 50.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onDismiss) { Text("Dismiss") }
            Spacer(Modifier.width(8.dp))
            Button(onClick = { onConfirm(monsterName ?: "") }) { Text("OK") }
        }
    }
}
