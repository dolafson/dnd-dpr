package com.vikinghelmet.dnd.dprapp.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import com.vikinghelmet.dnd.dprapp.DprViewModel


@Composable
fun StatHalfBlock(a1: AbilityType, a2: AbilityType, a3: AbilityType,
                  modifier1: Modifier, modifier2: Modifier,
                  viewModel: DprViewModel, onValueChanged: (Int) -> Unit)
{
    Column(modifier = modifier1) {
        Text(a1.toShortName())  // short name for display, full name for stat lookup
        Text(a2.toShortName())
        Text(a3.toShortName())
    }
    Column(modifier = modifier2) {
        NumericMenu(a1.name, viewModel, onValueChanged)
        NumericMenu(a2.name, viewModel, onValueChanged)
        NumericMenu(a3.name, viewModel, onValueChanged)
    }
}

@Composable
fun StatBlockDisplay(viewModel: DprViewModel, onValueChanged: (Int) -> Unit)
{
    Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
        StatHalfBlock( AbilityType.Strength, AbilityType.Dexterity, AbilityType.Constitution,
            Modifier.padding(start = 0.dp), Modifier.padding(start = 20.dp), viewModel, onValueChanged,)

        StatHalfBlock(AbilityType.Intelligence, AbilityType.Wisdom, AbilityType.Charisma,
            Modifier.padding(start = 60.dp), Modifier.padding(start = 20.dp), viewModel, onValueChanged,)
    }
}


// ====================================================================================
// TODO: delete the rest of this, if/when we fully give up on 3 columns per stat

/*
@Composable
fun StatDisplayNotEditable(stats: StatBlock, label: String) {
    if (stats == null || stats.str == 0) {
        Text("?")
    }
    else {
        Text(stats.getValue(label).toString())
    }
}

@Composable
fun StatModDisplay(stats: StatBlock, label: String, onValueChanged: (Int) -> Unit) {
    NumericMenu(0,20-stats.getValue(label),0,{ newValue ->
        stats.setValue (label, newValue)
        onValueChanged (newValue)
    } )
}

@Composable
fun StatHalfBlockWith3Columns(stats: StatBlock, editable: Boolean, onValueChanged: (Int) -> Unit,
                              label1: String, label2: String, label3: String,
                              modifier1: Modifier, modifier2: Modifier)
{
    Column(modifier = modifier1) {
        Text(label1)
        Text(label2)
        Text(label3)
    }
    Column(modifier = modifier2) {
        StatDisplayNotEditable(stats, label1)
        StatDisplayNotEditable(stats, label2)
        StatDisplayNotEditable(stats, label3)
    }
    if (editable && stats != null && stats.str >0) {
        Column(modifier = modifier2) {
            StatModDisplay(stats, label1, onValueChanged)
            StatModDisplay(stats, label2, onValueChanged)
            StatModDisplay(stats, label3, onValueChanged)
        }
    }
}
*/
