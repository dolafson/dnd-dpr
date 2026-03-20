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

@Composable
fun FeatMenu(character: Character,
             fightingStyleOnly: Boolean = false,
             onValueChanged: (String, String, String) -> Unit)
{
    var selectedFeat = remember { mutableStateOf("") }
    var asi1 = remember { mutableStateOf("") }
    var asi2 = remember { mutableStateOf("") }

    val featNamesWithColor = FeatEligibility.getListByCharacter(character).filter {
        !fightingStyleOnly || it.isFightingStyle
    }.map {
        Pair(it.getNameWithWS(), if (it.fullSupport) Color.Blue else Color.LightGray)
    }

    val feat = Feat.entries.find { selectedFeat.value == it.getNameWithWS() }

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
            BasicTextMenu(featNamesWithColor, { s ->
                println("featMenu changed, new feat = $s")
                selectedFeat.value = s
                asi1.value = ""
                asi2.value = ""
                onValueChanged(selectedFeat.value, asi1.value, asi2.value)
            })
            if (asiCount > 0) {
                BasicTextMenu(asiChoices, { s ->
                    println("featMenu changed, new asi1 = $s")
                    asi1.value = s
                    onValueChanged(selectedFeat.value, asi1.value, asi2.value)
                })
            }
            if (asiCount == 2) {
                BasicTextMenu(asiChoices, { s ->
                    println("featMenu changed, new asi2 = $s")
                    asi2.value = s
                    onValueChanged(selectedFeat.value, asi1.value, asi2.value)
                })
            }
        }
    }
}