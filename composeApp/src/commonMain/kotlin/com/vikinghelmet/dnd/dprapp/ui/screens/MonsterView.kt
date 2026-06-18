package com.vikinghelmet.dnd.dprapp.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vikinghelmet.dnd.dpr.action.Combatant
import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import com.vikinghelmet.dnd.dpr.monsters.Monster
import com.vikinghelmet.dnd.dpr.util.Movement
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
@Composable
//@Preview
fun MonsterView(combatant: Combatant?)// viewModel: DprViewModel, onTeamA: Boolean)
{
  //  var combatant by remember { mutableStateOf(viewModel.getCombatant(onTeamA)) }
    HorizontalDivider(modifier = Modifier.padding(top = 20.dp), thickness = 2.dp)

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
            } else {
                val m = combatant!!
                Text(m.getAC().toString())
                Text(m.getHP().toString())
                Text(m.getSpeed(Movement.walk).toString())
                Text(if (m is Monster) m.size else "TODO")
            }
        }
    }

    HorizontalDivider(modifier = Modifier.padding(top = 20.dp), thickness = 2.dp)

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
