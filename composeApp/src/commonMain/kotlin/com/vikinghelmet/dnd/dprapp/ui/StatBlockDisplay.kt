package com.vikinghelmet.dnd.dprapp.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import com.vikinghelmet.dnd.dprapp.DprViewModel

/*
@Composable
fun StatHalfBlock(a1: AbilityType, a2: AbilityType, a3: AbilityType,
                  modifier1: Modifier, modifier2: Modifier,
                  numericRangeMap: NumericRangeMap, onValueChanged: (Int) -> Unit)
{
    Column(modifier = modifier1) {
        Text(a1.toShortName())  // short name for display, full name for stat lookup
        Text(a2.toShortName())
        Text(a3.toShortName())
    }
    Column(modifier = modifier2) {
        NumericMenu(a1.name, numericRangeMap, onValueChanged)
        NumericMenu(a2.name, numericRangeMap, onValueChanged)
        NumericMenu(a3.name, numericRangeMap, onValueChanged)
    }
}

@Composable
fun StatBlockDisplayOld(viewModel: DprViewModel, onValueChanged: (Int) -> Unit)
{
    Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
        StatHalfBlock( AbilityType.Strength, AbilityType.Dexterity, AbilityType.Constitution,
            Modifier.padding(start = 0.dp), Modifier.padding(start = 20.dp), viewModel.getNumericRangeMap(), onValueChanged,)

        StatHalfBlock(AbilityType.Intelligence, AbilityType.Wisdom, AbilityType.Charisma,
            Modifier.padding(start = 60.dp), Modifier.padding(start = 20.dp), viewModel.getNumericRangeMap(), onValueChanged,)
    }
}

 */

@Composable
fun StatBlockDisplay(viewModel: DprViewModel, onValueChanged: (Int) -> Unit)
{
   //var viewModel = remember { mutableStateOf(viewModelIn) } // ???
    var rangeMap = remember(viewModel.uiState.value.numericRangeMap) { mutableStateOf(viewModel.uiState.value.numericRangeMap) }

    Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp))
    {
        Column(modifier = Modifier.padding(start = 0.dp)) {
            Text(AbilityType.Strength.toShortName())  // short name for display, full name for stat lookup
            Text(AbilityType.Dexterity.toShortName())
            Text(AbilityType.Constitution.toShortName())
        }
        Column(modifier = Modifier.padding(start = 20.dp)) {
            NumericMenu(AbilityType.Strength.name, rangeMap.value, onValueChanged)
            NumericMenu(AbilityType.Dexterity.name, rangeMap.value, onValueChanged)
            NumericMenu(AbilityType.Constitution.name, rangeMap.value, onValueChanged)
        }
        Column(modifier = Modifier.padding(start = 60.dp)) {
            Text(AbilityType.Intelligence.toShortName())
            Text(AbilityType.Wisdom.toShortName())
            Text(AbilityType.Charisma.toShortName())
        }
        Column(modifier = Modifier.padding(start = 20.dp)) {
            NumericMenu(AbilityType.Intelligence.name, rangeMap.value, onValueChanged)
            NumericMenu(AbilityType.Wisdom.name, rangeMap.value, onValueChanged)
            NumericMenu(AbilityType.Charisma.name, rangeMap.value, onValueChanged)
        }
    }
}
