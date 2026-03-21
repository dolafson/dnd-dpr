package com.vikinghelmet.dnd.dprapp.ui.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vikinghelmet.dnd.dpr.character.Character
import com.vikinghelmet.dnd.dpr.character.feats.Feat
import com.vikinghelmet.dnd.dpr.character.feats.FeatEligibility
import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import com.vikinghelmet.dnd.dprapp.data.PlanViewLevel

@Composable
fun FeatMenu(
    preselectedValues: PlanViewLevel,
    character: Character,
    fightingStyleOnly: Boolean = false,
    onValueChanged: (Feat, AbilityType?, AbilityType?) -> Unit
)
{
    var selectedFeat = remember { mutableStateOf<Feat?>(preselectedValues.feat) }
    var asi1 = remember { mutableStateOf<AbilityType?>(preselectedValues.asi1) }
    var asi2 = remember { mutableStateOf<AbilityType?>(preselectedValues.asi2) }

    val featNamesWithColor = FeatEligibility.getListByCharacter(character).filter {
        !fightingStyleOnly || it.isFightingStyle
    }.map {
        Pair(it.getNameWithWS(), if (it.fullSupport) Color.Blue else Color.LightGray)
    }

    val feat = Feat.entries.find { it == selectedFeat.value }

    val asiCount = if (feat == null) 0
        else if (Feat.AbilityScoreIncrease == feat) 2
        else if (feat.asiChoices.isNotEmpty()) 1
        else 0

    val asiChoices = if (asiCount == 0) emptyList() else feat!!.asiChoices.map { Pair(it.name,Color.Black) }.toList()

    Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
        Column {
            Text("Feat")
            for (i in 1..asiCount) {
                Text("Ability Score")
            }
        }
        Column (modifier = Modifier.padding(start = 20.dp)) {
            val priorFeatName = preselectedValues.feat?.name ?: ""
            BasicTextMenu(priorFeatName, featNamesWithColor, 200.dp, 200.dp) { s ->
                //println("featMenu changed, new feat = $s")
                selectedFeat.value = Feat.fromNameWithWS(s)
                asi1.value = null
                asi2.value = null
                onValueChanged(selectedFeat.value!!, asi1.value, asi2.value)
            }
            if (asiCount > 0) {
                val priorASI = preselectedValues.asi1?.name ?: ""
                BasicTextMenu(priorASI, asiChoices, 200.dp, 200.dp) { s ->
                    //println("featMenu changed, new asi1 = $s")
                    asi1.value = AbilityType.valueOf(s)
                    onValueChanged(selectedFeat.value!!, asi1.value, asi2.value)
                }
            }
            if (asiCount == 2) {
                val priorASI = preselectedValues.asi2?.name ?: ""
                BasicTextMenu(priorASI, asiChoices, 200.dp, 200.dp) { s ->
                    //println("featMenu changed, new asi2 = $s")
                    asi2.value = AbilityType.valueOf(s)
                    onValueChanged(selectedFeat.value!!, asi1.value, asi2.value)
                }
            }
        }
    }
}