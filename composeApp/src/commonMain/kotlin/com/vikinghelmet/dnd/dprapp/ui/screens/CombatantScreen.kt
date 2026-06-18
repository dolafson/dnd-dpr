package com.vikinghelmet.dnd.dprapp.ui.screens

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.vikinghelmet.dnd.dpr.action.Combatant
import com.vikinghelmet.dnd.dpr.character.PlayerCharacter
import com.vikinghelmet.dnd.dpr.editable.EditableFields
import com.vikinghelmet.dnd.dpr.editable.EditablePlayerCharacter
import com.vikinghelmet.dnd.dpr.util.Globals
import com.vikinghelmet.dnd.dpr.util.Party
import com.vikinghelmet.dnd.dprapp.DprViewModel
import com.vikinghelmet.dnd.dprapp.ViewType
import com.vikinghelmet.dnd.dprapp.data.Loader
import com.vikinghelmet.dnd.dprapp.data.Loader.addEditableCharacter
import com.vikinghelmet.dnd.dprapp.data.Loader.getRemoteJson
import com.vikinghelmet.dnd.dprapp.ui.widgets.CombatantMenu
import dev.shivathapaa.logger.api.LogLevel
import dev.shivathapaa.logger.api.LoggerFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.uuid.ExperimentalUuidApi


fun isNotEmptyAndNotInList(textFieldState: TextFieldState, options: List<Combatant>) =
    textFieldState.text.isNotBlank() && !options.any {it.getName() == textFieldState.text }

fun isCharacterInList(textFieldState: TextFieldState, options: List<Combatant>) =
    textFieldState.text.isNotBlank() && options.any {it.getName() == textFieldState.text && it is PlayerCharacter }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
@Composable
//@Preview
fun CombatantScreen(viewModel: DprViewModel, navHostController: NavHostController, onTeamA: Boolean)
{
    val logger = LoggerFactory.get("com.vikinghelmet.dnd.dprapp.ui.screens.CombatantScreen")

    var combatant by remember { mutableStateOf(viewModel.getCombatant(onTeamA)) }
    var combatantName by rememberSaveable { mutableStateOf("") }
    val textFieldState = rememberTextFieldState()

    var currentProgress by remember { mutableFloatStateOf(5f) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val options = remember { mutableListOf<Combatant>() }

    LaunchedEffect(Unit) {
        options.clear()
        options.addAll(getCombatants()) //dprFiles.getEditableCharacterNameList())

        combatant = Globals.getMonsterOrNull(combatantName)
        if (combatant == null) {
            combatant = dprFiles.getEditableCharacter(combatantName)
        }

        if (combatantName.isNotBlank()) {
            textFieldState.setTextAndPlaceCursorAtEnd(combatantName)
        }
    }

    fun reset() {
        combatant = null
        textFieldState.setTextAndPlaceCursorAtEnd("")
    }

    suspend fun addPartyBackground(party: Party) {
        currentProgress = 5f
        delay(1)
        loading = true

        println("fetching party ...")
        delay(1)
        var count = 1
        val size = party.party.size
        party.party.forEach {
            val json = getRemoteJson(it)
            val added = addEditableCharacter(json!!)
            if (added != null) options.add(added)
            currentProgress = (count++ * 1f) / (size * 1f)
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
        else if (addText == "kaboom") {
            dprFiles.deleteAll()
            reset()
            options.clear()
            return
        }

        val oldCharacter = combatant as? EditablePlayerCharacter
        var newCharacter: EditablePlayerCharacter? = oldCharacter
        val currentText = textFieldState.text.toString()

        if (options.isNotEmpty() && !isUrlOrID(currentText) && !currentText.contains("/")) {
            val editableFields = EditableFields(currentText, oldCharacter!!, viewModel.getCharacterLevel())
            dprFiles.saveEditableCharacter(editableFields)
            newCharacter = EditablePlayerCharacter(oldCharacter, editableFields)
            viewModel.setMainCharacter(newCharacter)
            options.add(newCharacter)
        }
        else {
            logger.info { "loading, input text: $currentText" }
            val json = Loader.getRemoteJson(currentText)
            if (json.isNullOrBlank()) {
                logger.error { "addCharacter($currentText), json is null or blank" }
            }
            else if (json.contains("\"party\"")) {
                logger.info { "loading party: $json" }
                scope.launch { addPartyBackground(Json.decodeFromString(json)) }
                reset()
            }
            else {
                logger.info { "loading editable character: ${json.substring(0, 30) + "..."}" }
                val addResult = Loader.addEditableCharacter(json)
                if (addResult != null) {
                    options.add(addResult)
                    newCharacter = addResult
                    textFieldState.setTextAndPlaceCursorAtEnd(addResult.getName())
                }
            }
        }

        combatant = newCharacter
        //println("after adding new character(s), options = $options, combatant = ${combatant?.getName()}")
    }

    Column(
        modifier = Modifier
            .padding(20.dp)
            .safeContentPadding()
            .fillMaxSize()
            .combinedClickable(onClick = {}, onDoubleClick =  { navHostController.popBackStack() })
    ) {
        CombatantMenu(textFieldState, false, getCombatants()) { selectedMonster ->
            combatant = selectedMonster
            textFieldState.setTextAndPlaceCursorAtEnd(selectedMonster?.getName() ?: "")
        }

        Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {

            Button(
                enabled = isNotEmptyAndNotInList(textFieldState, options),
                onClick = { addCharacter(textFieldState.text.toString()) }
            ) { Text("Add") }

            Button(
                modifier = Modifier.padding(start = 20.dp),
                enabled = isCharacterInList(textFieldState, options),
                onClick = { navHostController.navigate(ViewType.plan.name) }
            ) { Text("Plan") }

            Button(
                modifier = Modifier.padding(start = 20.dp),
                enabled = isCharacterInList(textFieldState, options),
                onClick = {
                    val name = textFieldState.text.toString()
                    if (combatant == viewModel.getMainCharacter()) {
                        viewModel.setMainCharacter(null)
                    }
                    dprFiles.deleteEditableCharacter(name)
                    options.remove(combatant)
                    combatant = null
                    textFieldState.setTextAndPlaceCursorAtEnd("")
                }
            ) { Text("Del") }
        }

        if (loading) {
            Spacer(Modifier.height(20.dp))
            LinearProgressIndicator(progress = { currentProgress }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(20.dp))
        }

        if (combatant == null) {
            // do nothing
        }
        else if (combatant is EditablePlayerCharacter) {
            CharacterView(viewModel, combatant)
        }
        else {
            MonsterView(combatant)
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
