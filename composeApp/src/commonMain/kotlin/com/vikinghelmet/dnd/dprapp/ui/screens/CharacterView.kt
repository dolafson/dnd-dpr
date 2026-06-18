package com.vikinghelmet.dnd.dprapp.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vikinghelmet.dnd.dpr.action.Combatant
import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import com.vikinghelmet.dnd.dpr.editable.EditablePlayerCharacter
import com.vikinghelmet.dnd.dprapp.DprViewModel
import com.vikinghelmet.dnd.dprapp.ui.widgets.NumericMenu
import kotlin.uuid.ExperimentalUuidApi

fun highlightIncrease(val1: Int, val2: Int): Color = if (val1 == val2) Color.Black else Color.Blue


@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
@Composable
//@Preview
fun CharacterView(viewModel: DprViewModel, combatant: Combatant?)
{
    var modifyCounter: Int by remember { mutableStateOf(0) }

    val spellSelections = remember(modifyCounter, viewModel.getCharacterLevel()) {
        (combatant as? EditablePlayerCharacter)?.getSpellSelectionsBySpellLevel(viewModel.getCharacterLevel().current) ?: emptyMap()
    }

    val character = combatant as EditablePlayerCharacter
    val subclass = character.getSubclassName()

    Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
        Column { Text("Level") }
        Column(modifier = Modifier.padding(start = 20.dp)) {
            NumericMenu(viewModel.getCharacterLevel(), { newLevel ->
                character.editableFields.level = newLevel
                modifyCounter++
            })
        }
    }

    HorizontalDivider(modifier = Modifier.padding(top = 20.dp), thickness = 2.dp)

    Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
        Column {
            Text("Class")
            if (subclass != null) Text("Subclass")
            Text("Prof Bonus")
            if (character.getSpellAbilityType() != null) Text("Spell DC")
        }
        Column(modifier = Modifier.padding(start = 20.dp)) {
            Text(character.getClassName())
            if (subclass != null) Text(subclass)
//                    Text(character.getProficiencyBonus().toString())
//                    if (character.getSpellAbilityType() != null) Text(character.getSpellSaveDC().toString())

            var current = character.getProficiencyBonus()
            Text(
                text = current.toString(),
                color = highlightIncrease(character.from.getProficiencyBonus(), current)
            )

            if (character.getSpellAbilityType() != null) {
                current = character.getSpellSaveDC()
                Text(current.toString(), color = highlightIncrease(character.from.getSpellSaveDC(), current))
            }
        }
    }

    Spacer(modifier = Modifier.padding(top = 20.dp))

    // NOTE: resist the urge to refactor this stat block into common code shared with CharacterScreen
    // that refactoring only leads to misery and woe (mismanaged composable state)
    Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
        Column(modifier = Modifier.padding(start = 0.dp)) {
            Text(AbilityType.Strength.toShortName())
            Text(AbilityType.Dexterity.toShortName())
            Text(AbilityType.Constitution.toShortName())
        }
        Column(modifier = Modifier.padding(start = 20.dp)) {
            listOf(AbilityType.Strength, AbilityType.Dexterity, AbilityType.Constitution).forEach {
                val baselineScore = character.from.getAbilityScore(it)
                val currentScore = character.getAbilityScore(it)
                Text(text = (currentScore).toString(), color = highlightIncrease(baselineScore, currentScore))
            }
        }
        Column(modifier = Modifier.padding(start = 60.dp)) {
            Text(AbilityType.Intelligence.toShortName())
            Text(AbilityType.Wisdom.toShortName())
            Text(AbilityType.Charisma.toShortName())
        }
        Column(modifier = Modifier.padding(start = 20.dp)) {
            listOf(AbilityType.Intelligence, AbilityType.Wisdom, AbilityType.Charisma).forEach {
                val baselineScore = character.from.getAbilityScore(it)
                val currentScore = character.getAbilityScore(it)
                Text(text = (currentScore).toString(), color = highlightIncrease(baselineScore, currentScore))
            }
        }
    }

    if (character.getFeatList().isNotEmpty()) {
        HorizontalDivider(modifier = Modifier.padding(top = 20.dp), thickness = 2.dp)
        Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
            Column {
                Text("Feat", fontWeight = FontWeight.Bold)
                character.getFeatList().forEach { feat -> Text(feat.getNameWithWS()) }
            }
        }
    }

    for (selection in spellSelections) {
        if (selection.value.isNotEmpty()) {
            HorizontalDivider(modifier = Modifier.padding(top = 20.dp), thickness = 2.dp)
            Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
                Column {
                    val label = if (selection.key == 0) "Cantrips" else "Level ${selection.key} Spells"
                    Text(label, fontWeight = FontWeight.Bold)
                    for (spell in selection.value) {
                        Text(spell.name)
                    }
                }
            }
        }
    }

    if (character.getWeaponList().isNotEmpty()) {
        HorizontalDivider(modifier = Modifier.padding(top = 20.dp), thickness = 2.dp)
        Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
            Column {
                Text("Weapon", fontWeight = FontWeight.Bold)
                character.getWeaponList().distinct()
                    .forEach { weapon -> Text(weapon.name.replace(",.*".toRegex(), "")) }
            }
            Column(modifier = Modifier.padding(start = 20.dp)) {
                Text("Hit", fontWeight = FontWeight.Bold)
                character.getWeaponList().distinct()
                    .forEach { weapon -> Text("+" + character.getAttackBonus(weapon).toString()) }
            }
            Column(modifier = Modifier.padding(start = 20.dp)) {
                Text("Damage", fontWeight = FontWeight.Bold)
                character.getWeaponList().distinct().forEach { weapon ->
                    Text(weapon.getDamageList().toString())
                }
            }
        }
    }
}
