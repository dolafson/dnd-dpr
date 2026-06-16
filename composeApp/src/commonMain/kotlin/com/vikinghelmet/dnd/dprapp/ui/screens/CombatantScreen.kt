package com.vikinghelmet.dnd.dprapp.ui.screens

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import com.vikinghelmet.dnd.dpr.monsters.Monster
import com.vikinghelmet.dnd.dpr.util.Globals
import com.vikinghelmet.dnd.dpr.util.Movement
import com.vikinghelmet.dnd.dprapp.DprViewModel
import com.vikinghelmet.dnd.dprapp.ui.widgets.CombatantMenu

@OptIn(ExperimentalMaterial3Api::class)
@Composable
//@Preview
fun CombatantScreen(viewModel: DprViewModel, navHostController: NavHostController, onTeamA: Boolean)
{
    var combatant by remember { mutableStateOf(viewModel.getCombatant(onTeamA)) }
    var combatantName by rememberSaveable { mutableStateOf("") }
    val textFieldState = rememberTextFieldState()

    LaunchedEffect(Unit) {
//        combatantName = combatant?.getName() ?: ""
//        println("main monster: "+viewModel.getMainMonster())
//        println("monsterName: $combatantName")

        combatant = Globals.getMonsterOrNull(combatantName)
        if (combatant == null) {
            combatant = dprFiles.getEditableCharacter(combatantName)
        }

        if (combatantName.isNotBlank()) {
            textFieldState.setTextAndPlaceCursorAtEnd(combatantName)
        }
    }

    Column(
        modifier = Modifier
            .padding(20.dp)
            .safeContentPadding()
            .fillMaxSize()
            .combinedClickable(onClick = {}, onDoubleClick =  { navHostController.popBackStack() })
    ) {
        CombatantMenu(textFieldState, true) { selectedMonster ->
            combatant = selectedMonster
            textFieldState.setTextAndPlaceCursorAtEnd(selectedMonster?.getName() ?: "")
        }

        if (combatant == null) {
            // do nothing
        }
        //else if (combatant is PlayerCharacter) {
            // TODO: if combatant is PlayerCharacter, show UI components copied from CharacterScreen
        //}
        else {
            HorizontalDivider(modifier = Modifier.padding(top = 20.dp), thickness = 2.dp)//, color = Color.Blue)

            Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
                Column {
                    Text("Name")
                    Text("AC")
                    Text("HP")
                    Text("Speed")
                    Text("Size")
                }
                Column(modifier = Modifier.padding(start = 20.dp)) {
                    Text(if (combatant == null) "?" else combatant!!.getName())

                    if (combatant == null) {
                        Text("?")
                        Text("?")
                        Text("?")
                        Text("?")
                    }
                    else {
                        val m = combatant!!
                        Text(m.getAC().toString())
                        Text(m.getHP().toString())
                        Text(m.getSpeed(Movement.walk).toString())
                        Text(if (m is Monster) m.size else "TODO")
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
                    Text(text = (combatant?.getAbilityScore(AbilityType.Strength)).toString())
                    Text(text = (combatant?.getAbilityScore(AbilityType.Dexterity)).toString())
                    Text(text = (combatant?.getAbilityScore(AbilityType.Constitution)).toString())
                }
                Column(modifier = Modifier.padding(start = 60.dp)) {
                    Text("INT")
                    Text("WIS")
                    Text("CHA")
                }
                Column(modifier = Modifier.padding(start = 20.dp)) {
                    Text(text = (combatant?.getAbilityScore(AbilityType.Intelligence) ?: "?").toString())
                    Text(text = (combatant?.getAbilityScore(AbilityType.Wisdom) ?: "?").toString())
                    Text(text = (combatant?.getAbilityScore(AbilityType.Charisma) ?: "?").toString())
                }
            }
        }


        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 50.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = {
                navHostController.popBackStack()
            }) { Text("Dismiss") }

            Spacer(Modifier.width(8.dp))

            Button(onClick = {
                viewModel.setCombatant(combatant, onTeamA)
                saveSettings(viewModel)
                navHostController.popBackStack()
            }) { Text("OK") }
        }
    }
}
