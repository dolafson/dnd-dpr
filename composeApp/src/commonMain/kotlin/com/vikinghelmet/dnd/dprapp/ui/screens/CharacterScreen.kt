package com.vikinghelmet.dnd.dprapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vikinghelmet.dnd.dpr.CmdTest
import com.vikinghelmet.dnd.dpr.character.Character
import com.vikinghelmet.dnd.dpr.modified.CharacterOverrides
import com.vikinghelmet.dnd.dpr.modified.ModifiedCharacter
import com.vikinghelmet.dnd.dpr.modified.StatBlock
import com.vikinghelmet.dnd.dpr.util.CharacterListItem
import com.vikinghelmet.dnd.dpr.util.DprSettings
import com.vikinghelmet.dnd.dprapp.ui.NumericMenu
import com.vikinghelmet.dnd.dprapp.ui.StatBlockDisplay
import com.vikinghelmet.dnd.dprapp.ui.dprFiles
import kotlinx.coroutines.runBlocking
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

var character: Character? = null

fun initCharacter(settings: DprSettings) {
    val match = getMatchingCharacterItem(settings)
    if (match != null) {
        character = dprFiles.getCharacter(match.remoteId)
    }
}

fun getMatchingCharacterItem(settings: DprSettings): CharacterListItem? {
    for (item in settings.characterList) {
        if (item.name == settings.characterName) {
            return item
        }
    }
    return null
}

fun isUrlOrID(str: String): Boolean {
    return str.startsWith("http://") || str.startsWith("https://") || str.toIntOrNull() != null
}

fun addCharacterToList (character: Character, isLocal: Boolean, options: MutableList<CharacterListItem>, settings: DprSettings)
{
    val item = CharacterListItem(character.characterData.id.toString(), character.getName(), isLocal)
    options.add(item)
    settings.characterList.add(item)
    settings.characterName = item.name
}

fun loadCharacter(selectedOption: MutableState<CharacterListItem>, urlOrId: String, options: MutableList<CharacterListItem>, settings: DprSettings, statBlock: StatBlock): String {
    var remoteId: String? = selectedOption.value.remoteId

    if (remoteId!!.isNotBlank() && selectedOption.value.isLocal) {
        println("loadCharacter: local overrides")
        // if user selected LOCAL entry from menu, load character from local storage (w/ overrides)
        val baseline  = dprFiles.getCharacter(remoteId)
        val overrides = dprFiles.getModifiedCharacter(selectedOption.value.name)

        if (baseline != null && overrides != null) {
            settings.characterName = selectedOption.value.name
            character = ModifiedCharacter(baseline, overrides)
        }
    }
    else if (remoteId!!.isNotBlank() && dprFiles.getCharacterList().contains(remoteId))
    {
        println("loadCharacter: local, no overrides")
        // if user selected REMOTE entry from menu, load character from local storage
        character = dprFiles.getCharacter(remoteId)
        if (character != null) {
            settings.characterName = selectedOption.value.name
        }
    }
    else {
        println("loadCharacter: URL or ID")
        // user hand-entered a characterID / URL ... first check for validity
        remoteId = CmdTest.getCharacterId(urlOrId)
        if (remoteId != null) {
            // then update the ID, update menu, and fetch from remote storage
            // on a good fetch, update local storage as well as the menu
            try {
                runBlocking {
                    character = if (urlOrId.contains("http") && !urlOrId.contains("dndbeyond")) {
                        // content hosted somewhere other than dndbeyond
                        CmdTest.getRemoteCharacterByUrl(urlOrId)
                    }
                    else {
                        // default: dndbeyond
                        CmdTest.getRemoteCharacter(remoteId)
                    }
                }
                if (character != null) {
                    dprFiles.saveCharacter(character!!, remoteId)
                    addCharacterToList(character!!, true, options, settings)
                }
            } catch (e: Exception) {
                //println("Error getting character, $e")
                return "CharacterID invalid / not found"
            }
        }
    }

    if (character != null) {
        println("loadCharacter: stat block before: $statBlock")
        statBlock.copyValues(character!!.getStatBlock())
        println("loadCharacter: stat block after: $statBlock")
        return character!!.toStringWeapons()+"\n"+character!!.toStringFeats()
    }
    return ""
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
@Composable
//@Preview
fun CharacterScreen(settings: DprSettings,
                    onDismiss: () -> Unit,
                    onConfirm: (String) -> Unit,
                    onReset: () -> Unit)
{
    var outputText by remember { mutableStateOf("") }

    var level by remember { mutableStateOf(if (character != null) character!!.getLevel() else 0) }

//    var statBlock by remember { mutableStateOf(if (character != null) character!!.getStatBlock() else
//        StatBlock(0,0,0,0,0,0)) }
    var statBlock= if (character != null) character!!.getStatBlock() else StatBlock(0,0,0,0,0,0)

    var showNameAlert by remember { mutableStateOf(false) }
    var modified by remember { mutableStateOf(false) }

    val options = remember { mutableListOf<CharacterListItem>() }
    var selectedOption = remember { mutableStateOf(CharacterListItem("","",false)) }

    var expanded by remember { mutableStateOf(false) }
    val textFieldState = rememberTextFieldState()

    LaunchedEffect(Unit) {
        options.clear()
        options.addAll (settings.characterList)

        println("character list = "+options)
        // println("CharacterScreen LaunchedEffect, begin; characterId = " + characterId)
        val match = getMatchingCharacterItem(settings)
        if (match != null) {
            selectedOption.value.copyValues(match)
            textFieldState.setTextAndPlaceCursorAtEnd(selectedOption.value.name)
        }
        println("match = $match")
        println("selectedOption = $selectedOption")
    }

    Column(
        modifier = Modifier
            .padding(20.dp)
            .safeContentPadding()
            .fillMaxSize(),
    ) {
        Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                TextField(
                    state = textFieldState,
                    label = { Text("Select/Add Character") },
                    //value = {
                    //    Text(if (selectedOption.value.name.isNotBlank()) selectedOption.value.name else "Select/Add Character")
                    //},
                    readOnly = false,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded,
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.SecondaryEditable))
                    },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryEditable)
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    //println("ExposedDropdownMenu: options $options")

                    options.forEach { option ->
                        //println("DropdownMenuItem: option $option")

                        DropdownMenuItem(
                            text = { Text(option.name, color = MaterialTheme.colorScheme.onSurface) },
                            onClick = {
                                selectedOption.value.copyValues(option)
                                textFieldState.setTextAndPlaceCursorAtEnd(option.name)
                                outputText = loadCharacter(selectedOption, textFieldState.text.toString(), options, settings, statBlock)
                                expanded = false

                                // use navigation to reload the entire page ... this will reset the editable boxes ...
                                // this feels like a hack, maybe later we'll find a better way
                                onReset()
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }
        }

        Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
            Button(
                enabled = (textFieldState.text.isNotBlank() &&
                            isUrlOrID(textFieldState.text.toString()) &&
                            !options.map { op -> op.name }.contains(textFieldState.text.toString())),
                onClick = {
                    outputText =  loadCharacter(selectedOption, textFieldState.text.toString(), options, settings, statBlock)
            }) { Text("Add") }

            Button(
                modifier = Modifier.padding(start = 20.dp),
                enabled = modified,
                onClick = {
                    println("TODO: Save character, baseline = ${selectedOption.value.name}")

                    for (option in options) {
                        if (option.name == textFieldState.text) {
                            println("warning: name matches an existing character ${option.name}")
                            showNameAlert = true
                        }
                    }
                    if (!showNameAlert) {
                        val characterOverrides = CharacterOverrides(Uuid.random().toString(),
                            character!!.characterData.id!!, level,
                            textFieldState.text.toString(), statBlock)
                        println("overrides = $characterOverrides")
                        dprFiles.saveModifiedCharacter(characterOverrides)

                        addCharacterToList(character!!, false, options, settings)
                    }
                }
            ) { Text("Save") }

            if (showNameAlert) {
                AlertDialog(
                    text = { Text("please choose a new name") },
                    onDismissRequest = {},
                    confirmButton = { TextButton(onClick = { showNameAlert = false }) { Text("OK") } },
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(top = 20.dp), thickness = 2.dp)//, color = Color.Blue)

        // ID/Name are arguably redundant ...
        // FieldValue("ID", (character?.characterData?.id ?: "?" ).toString())
        // FieldValue("Name", character?.characterData?.name ?: "?" )

        Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
            Column {
                Text("Level")
                Text("Proficiency Bonus")
                Text("Spell Save DC")
                Text("Spell Ability")
                // currently unable to calculate: AC, HP
            }
            Column(modifier = Modifier.padding(start = 20.dp)) {
                if (character == null) {
                    Text("?")
                } else {
                    val min = character!!.getLevel()
                    NumericMenu(min, 20, min, { level = it; modified = true })
/*
                    Row() {
                        Text(min.toString(), modifier = Modifier.padding(end = 20.dp))
                        NumericMenu(0, 20 - min, level, { level = it; modified = true })
                    } */
                }

                Text((character?.getProficiencyBonus() ?: "?" ).toString())
                Text((character?.getSpellSaveDC() ?: "?" ).toString())
                Text((character?.getSpellAbilityType() ?: "?" ))
            }
        }

        HorizontalDivider(modifier = Modifier.padding(top = 20.dp), thickness = 2.dp)//, color = Color.Blue)

        if (character != null) {
            println ("redraw stats, statBlock = $statBlock")
            StatBlockDisplay(statBlock,true, { newValue ->
                modified = true
                println("stat changed: $newValue")
            } )
        }

        HorizontalDivider(modifier = Modifier.padding(top = 20.dp), thickness = 2.dp)//, color = Color.Blue)

        if (character != null && character!!.getWeaponList().isNotEmpty())
        {
            Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
                Column {
                    Text("Weapon", fontWeight = FontWeight.Bold)
                    character!!.getWeaponList().distinct().forEach { weapon -> Text(weapon.name) }
                }
                Column (modifier = Modifier.padding(start = 20.dp)) {
                    Text("Hit", fontWeight = FontWeight.Bold)
                    character!!.getWeaponList().distinct().forEach { weapon -> Text("+"+character!!.getAttackBonus(weapon).toString()) }
                }
                Column (modifier = Modifier.padding(start = 20.dp)) {
                    Text("Damage", fontWeight = FontWeight.Bold)
                    character!!.getWeaponList().distinct().forEach { weapon -> Text(
                        if (character!!.getDamageBonus(weapon, false) == 0) {
                            weapon.damage!!
                        } else {
                            // TODO: BA
                            weapon.damage!! +" + " +character!!.getDamageBonus(weapon, false).toString()
                        }
                    ) }
                }
            }
        }

        if (character != null && character!!.getFeatList().isNotEmpty()) {

            HorizontalDivider(modifier = Modifier.padding(top = 20.dp), thickness = 2.dp)//, color = Color.Blue)

            Row(modifier = Modifier.padding(start = 20.dp, top = 10.dp)) {
                Column {
                    Text("Feat", fontWeight = FontWeight.Bold)
                    character!!.getFeatList().forEach { feat -> Text(feat.definition.name) }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onDismiss) { Text("Dismiss") }
            Spacer(Modifier.width(8.dp))
            Button( onClick = { onConfirm(selectedOption.value.name) }) { Text("OK") }
        }
    }
}
