package com.vikinghelmet.dnd.dprapp.ui.screens

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import com.vikinghelmet.dnd.dpr.editable.EditableCharacter
import com.vikinghelmet.dnd.dpr.editable.EditableFields
import com.vikinghelmet.dnd.dpr.util.Globals
import com.vikinghelmet.dnd.dprapp.DprViewModel
import com.vikinghelmet.dnd.dprapp.ViewType
import com.vikinghelmet.dnd.dprapp.data.Loader
import com.vikinghelmet.dnd.dprapp.data.Loader.addEditableCharacter
import com.vikinghelmet.dnd.dprapp.ui.widgets.CharacterMenu
import com.vikinghelmet.dnd.dprapp.ui.widgets.NumericMenu
import dev.shivathapaa.logger.api.LogLevel
import dev.shivathapaa.logger.api.LoggerFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi

fun isUrlOrID(str: String): Boolean {
    return str.startsWith("http://") || str.startsWith("https://") || str.toIntOrNull() != null
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
@Composable
fun CharacterScreen(viewModel: DprViewModel, navHostController: NavHostController)
{
    val logger = LoggerFactory.get("com.vikinghelmet.dnd.dprapp.ui.screens.CharacterScreen")

    var viewCharacter: EditableCharacter? = viewModel.getCurrentCharacter()
    // val focusManager = LocalFocusManager.current

    var currentProgress by remember { mutableFloatStateOf(5f) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope() // Create a coroutine scope

    var modifyCounter: Int by remember { mutableStateOf(0) }

    val options = remember { mutableListOf<String>() }
    var expanded by remember { mutableStateOf(false) }
    val textFieldState = rememberTextFieldState()

    val spellSelections = remember(modifyCounter, viewModel.getCharacterLevel()) {
        // println("modifyCounter: $modifyCounter")
        viewCharacter?.getSpellSelectionsBySpellLevel(viewModel.getCharacterLevel().current) ?: emptyMap()
    }


    LaunchedEffect(Unit) {
        options.clear()
        options.addAll(dprFiles.getEditableCharacterList())

        if (viewModel.getCurrentCharacter() != null) {
            println("CharacterScreen: LaunchedEffect: set char name: ${viewModel.getCurrentCharacter()!!.getName()}")

            textFieldState.setTextAndPlaceCursorAtEnd(viewModel.getCurrentCharacter()!!.getName())
        }
    }


    fun highlightIncrease(val1: Int, val2: Int): Color = if (val1 == val2) Color.Black else Color.Blue

    fun reset(viewModel: DprViewModel, textFieldState: TextFieldState) {
        viewModel.setCurrentCharacter(null)
        textFieldState.setTextAndPlaceCursorAtEnd("")
    }

    suspend fun addPartyBackground() {
        currentProgress = 5f
        delay(1)
        loading = true

        println("fetching party ...")
        delay(1)
        val party = Loader.getParty()
        var count = 1
        // when loading party, update progress ...
        party.forEach {
            val added = addEditableCharacter("https://www.vikinghelmet.com/dnd/party/$it.json")
            if (added != null) options.add(added.getName())
            currentProgress = (count++ * 1f) / (party.size * 1f)
            delay(1)
        }
        loading = false
    }

    fun addCharacter(addText: String) {
        if (addText == "debug") {
            println("attempting to set debug logging")
            Globals.initLogger(LogLevel.DEBUG)
            logger.info { "debug logging enabled" }
            textFieldState.setTextAndPlaceCursorAtEnd("")
            return
        }
        else if (addText == "party") {
            scope.launch { addPartyBackground() }
            reset(viewModel, textFieldState)
        }
        else {
            val oldCharacter = viewCharacter
            var newCharacter: EditableCharacter? = viewCharacter // default: old -> new

            val currentText = textFieldState.text.toString()
            if (currentText == "kaboom") {
                dprFiles.deleteAll()
                viewModel.setCurrentCharacter(null)
                reset(viewModel,textFieldState)
                options.clear()
            }
            else if (options.isNotEmpty() && !isUrlOrID(currentText)) {
                // old character, new name
                val editableFields = EditableFields(currentText, oldCharacter!!, viewModel.getCharacterLevel())

                dprFiles.saveEditableCharacter(editableFields)
                newCharacter = EditableCharacter(oldCharacter!!, editableFields)
                viewModel.setMainCharacter(oldCharacter)

                if (!options.contains(currentText)) {
                    options.add(currentText)
                }
            }
            else if (isUrlOrID(currentText)) {
                val addResult = Loader.addEditableCharacter(currentText)
                if (addResult != null) {
                    options.add(addResult.getName())
                    viewModel.setCurrentCharacter(addResult)
                    textFieldState.setTextAndPlaceCursorAtEnd(addResult.getName())
                }
            }
            else {
                logger.warn { "addCharacter, not URL or ID ... how did we get here?" }
            }

            viewCharacter = newCharacter
        }

        println("after adding new character(s), options = $options, current = ${viewModel.getCurrentCharacter()}")
    }


    Column(
        modifier = Modifier
            .padding(20.dp)
            .safeContentPadding()
            .fillMaxSize()
            .combinedClickable(onClick = {}, onDoubleClick =  { navHostController.popBackStack() })
    ) {
        Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
            CharacterMenu(
                "Select/Add Character", dprFiles.getEditableCharacterList(), textFieldState, false,
                { addText ->
                    addCharacter(addText)
                },
                { selectedOption ->
                    viewModel.setCurrentCharacter(dprFiles.getEditableCharacter(selectedOption))
                    println("from menu selection, set main character = ${viewModel.getCurrentCharacter()!!.getName()}")
                })
        }

        Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
            Button(
                enabled = (textFieldState.text.isNotBlank() && !options.contains(textFieldState.text.toString())),
                onClick = { addCharacter(textFieldState.text.toString()) }
            ) {  Text("Add") }

            Button(
                modifier = Modifier.padding(start = 20.dp),
                enabled = (textFieldState.text.isNotBlank() && options.contains(textFieldState.text.toString())),
                onClick = {
                    navHostController.navigate(ViewType.plan.name)
                }
            ) { Text("Plan") } // running out of room on ios screen width

            Button(
                modifier = Modifier.padding(start = 20.dp),
                enabled = (textFieldState.text.isNotBlank() && options.contains(textFieldState.text.toString())),
                onClick = {
                    val name = textFieldState.text.toString()
                    if (viewCharacter == viewModel.getMainCharacter()) {
                        viewModel.setMainCharacter(null)
                    }
                    dprFiles.deleteEditableCharacter(name)

                    options.remove(name)
                    viewModel.setCurrentCharacter(null)
                    textFieldState.setTextAndPlaceCursorAtEnd("")
                }
            ) { Text("Del") } // running out of room on ios screen width
        }

        if (loading) {
            Spacer(Modifier.height(20.dp))
            LinearProgressIndicator(
                progress = { currentProgress }, modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(20.dp))
        }

        if (viewCharacter != null) {
            val character: EditableCharacter = viewCharacter!!

            Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
                Column {
                    Text("Level")
                }
                Column(modifier = Modifier.padding(start = 20.dp)) {
                    NumericMenu(viewModel.getCharacterLevel(), { newLevel ->
                        character.editableFields.level = newLevel
                        modifyCounter++;
                    })
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(top = 20.dp), thickness = 2.dp)

        // everything below here - except for Dismiss button - requires a valid character ... AND is read-only

        if (viewCharacter != null) {
            val character: EditableCharacter = viewCharacter!!
            val subclass = character.getSubclassName()

            Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
                Column {
                    Text("Class")
                    if (subclass != null) {
                        Text("Subclass")
                    }

                    Text("Prof Bonus")

                    if (character.getSpellAbilityType() != "n/a") {
                        Text("Spell DC")
                    }
                    // currently unable to calculate: AC, HP
                }
                Column(modifier = Modifier.padding(start = 20.dp)) {
                    Text(text = character.getClassName())
                    if (subclass != null) {
                        Text(text = subclass)
                    }

                    var current = character.getProficiencyBonus()
                    Text(
                        text = current.toString(),
                        color = highlightIncrease(character.from.getProficiencyBonus(), current)
                    )

                    if (character.getSpellAbilityType() != "n/a") {
                        current = character.getSpellSaveDC()
                        Text(current.toString(), color = highlightIncrease(character.from.getSpellSaveDC(), current))
                    }
                }
            }

            Spacer(modifier = Modifier.padding(top = 20.dp))

            // NOTE: resist the urge to refactor this stat block into common code shared with MonsterScreen
            // that refactoring only leads to misery and woe (mismanaged composable state)
            Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp))
            {
                Column(modifier = Modifier.padding(start = 0.dp)) {
                    Text(AbilityType.Strength.toShortName())  // short name for display, full name for stat lookup
                    Text(AbilityType.Dexterity.toShortName())
                    Text(AbilityType.Constitution.toShortName())
                }
                Column(modifier = Modifier.padding(start = 20.dp)) {
                    listOf(AbilityType.Strength, AbilityType.Dexterity, AbilityType.Constitution).forEach {
                        val baselineScore = character.from.getModifiedAbilityScore(it)
                        val currentScore = character.getModifiedAbilityScore(it)
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
                        val baselineScore = character.from.getModifiedAbilityScore(it)
                        val currentScore = character.getModifiedAbilityScore(it)
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

            //val spellSelections = character.getSpellSelectionsBySpellLevel(viewModel.getCharacterLevel().current)

            for (selection in spellSelections) {
                if (selection.value.isNotEmpty()) {
                    HorizontalDivider(modifier = Modifier.padding(top = 20.dp), thickness = 2.dp)
                    Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
                        Column {
                            val label = if (selection.key == 0) "Cantrips" else "Level ${selection.key} Spells"
                            Text(label, fontWeight = FontWeight.Bold)

                            for (spell in selection.value) {
                                // val color = if (entry.value < character.getLevel()) Color.Black else Color.Blue
                                Text(spell.name)
                            }
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(top = 20.dp), thickness = 2.dp)

            if (character.getWeaponList().isNotEmpty()) {
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
                            Text(
                                if (character.getDamageBonus(weapon, false) == 0) {
                                    weapon.damage!!
                                } else {
                                    // TODO: BA
                                    weapon.damage!! + " + " + character.getDamageBonus(weapon, false).toString()
                                }
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = {
                navHostController.popBackStack()
            }) { Text("Dismiss") }

            Spacer(Modifier.width(8.dp))

            Button(onClick = {
                viewModel.setMainCharacter(viewCharacter!!)
                saveSettings(viewModel)
                navHostController.popBackStack()
            }) { Text("OK") }
        }
    }
}



