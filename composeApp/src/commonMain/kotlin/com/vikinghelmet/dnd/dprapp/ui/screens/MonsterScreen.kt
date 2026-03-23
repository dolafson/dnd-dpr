package com.vikinghelmet.dnd.dprapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vikinghelmet.dnd.dpr.util.Globals
import com.vikinghelmet.dnd.dprapp.DprViewModel
import com.vikinghelmet.dnd.dprapp.ui.widgets.MonsterMenu

@OptIn(ExperimentalMaterial3Api::class)
@Composable
//@Preview
fun MonsterScreen(viewModel: DprViewModel,
                  onDismiss: () -> Unit,
                  onConfirm: (String) -> Unit)
{
    var monster by remember { mutableStateOf(viewModel.getCurrentMonster()) }
    val textFieldState = rememberTextFieldState()

    var monsterName by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        println("main monster: "+viewModel.getMainMonster())

        viewModel.setCurrentMonster(viewModel.getMainMonster())
        println("current monster: "+viewModel.getCurrentMonster())

        monsterName = viewModel.getCurrentMonster()?.name ?: ""
        println("monsterName: $monsterName")

        monster = Globals.getMonsterOrNull(monsterName)

        if (monsterName.isNotBlank()) {
            textFieldState.setTextAndPlaceCursorAtEnd(monsterName)
        }
    }

    Column(
        modifier = Modifier
            .padding(20.dp)
            .safeContentPadding()
            .fillMaxSize()
    ) {
        MonsterMenu(textFieldState, true) { selectedMonster ->
            monster = selectedMonster
            viewModel.setCurrentMonster(selectedMonster)
            textFieldState.setTextAndPlaceCursorAtEnd(selectedMonster?.name ?: "")
        }

        HorizontalDivider(modifier = Modifier.padding(top = 20.dp), thickness = 2.dp)//, color = Color.Blue)

        Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
            Column {
                Text("Monster Name")
                Text("Armor Class")
                Text("Hit Points")
                Text("Speed")
                Text("Size")
            }
            Column(modifier = Modifier.padding(start = 20.dp)) {
                Text(if (monster == null) "?" else monster!!.name)

                if (viewModel.getCurrentMonster() == null) {
                    Text("?")
                    Text("?")
                    Text("?")
                    Text("?")
                }
                else {
                    val m = viewModel.getCurrentMonster()!!
                    //Text((monster?.properties?.dataAcNum ?: "?").toString())
                    Text((m.properties.dataAcNum).toString())
                    Text((m.properties.HP))
                    Text((m.properties.Speed))
                    Text((m.properties.Size))
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(top = 20.dp), thickness = 2.dp)//, color = Color.Blue)

        // NOTE: resist the urge to refactor this stat block into common code shared with CharacterScreen
        // that refactoring only leads to misery and woe (mismanaged composable state)
        Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp))
        {
            Column(modifier = Modifier.padding(start = 0.dp)) {
                Text("STR")
                Text("DEX")
                Text("CON")
            }
            Column(modifier = Modifier.padding(start = 20.dp)) {
                Text(text = (monster?.properties?.STR ?: "?").toString())
                Text(text = (monster?.properties?.DEX ?: "?").toString())
                Text(text = (monster?.properties?.CON ?: "?").toString())
            }
            Column(modifier = Modifier.padding(start = 60.dp)) {
                Text("INT")
                Text("WIS")
                Text("CHA")
            }
            Column(modifier = Modifier.padding(start = 20.dp)) {
                Text(text = (monster?.properties?.INT ?: "?").toString())
                Text(text = (monster?.properties?.WIS ?: "?").toString())
                Text(text = (monster?.properties?.CHA ?: "?").toString())
            }
        }


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
