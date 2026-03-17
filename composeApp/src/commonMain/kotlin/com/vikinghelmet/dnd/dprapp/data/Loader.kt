package com.vikinghelmet.dnd.dprapp.data

import androidx.compose.runtime.MutableState
import com.vikinghelmet.dnd.dpr.CmdTest
import com.vikinghelmet.dnd.dpr.character.Character
import com.vikinghelmet.dnd.dpr.modified.ModifiedCharacter
import com.vikinghelmet.dnd.dpr.monsters.Monster
import com.vikinghelmet.dnd.dpr.util.CharacterListItem
import com.vikinghelmet.dnd.dpr.util.DprSettings
import com.vikinghelmet.dnd.dpr.util.Globals
import com.vikinghelmet.dnd.dprapp.ui.dprFiles
import kotlinx.coroutines.runBlocking

object Loader {

    fun getMonster(settings: DprSettings): Monster? {
        return getMonster(settings.monsterName)
    }

    fun getMonster(name: String): Monster? {
        try {
            return Globals.getMonster(name)
        }
        catch (e: Exception) {
            println("unable to find monster with name = $name, $e")
            return null
        }
    }

    fun getCharacter(settings: DprSettings): Character? {
        var match: CharacterListItem? = null

        for (item in settings.characterList) {
            if (item.name == settings.characterName) {
                match = item
            }
        }
        if (match != null) {
            return dprFiles.getCharacter(match.remoteId)
        }
        return null
    }

    fun getCharacter(selectedOption: MutableState<CharacterListItem>): Character?
    {
        var result: Character? = null
        val remoteId: String = selectedOption.value.remoteId

        if (remoteId.isNotBlank() && selectedOption.value.isLocal) {
            val baseline  = dprFiles.getCharacter(remoteId)
            val overrides = dprFiles.getModifiedCharacter(selectedOption.value.name)
            if (baseline != null && overrides != null) {
                result = ModifiedCharacter(baseline, overrides)
            }
        }
        else if (remoteId.isNotBlank() && dprFiles.getCharacterList().contains(remoteId))
        {
            result = dprFiles.getCharacter(remoteId)
        }
        return result
    }

    fun addCharacter(selectedOption: MutableState<CharacterListItem>, urlOrId: String): Character?
    {
        var result: Character? = getCharacter(selectedOption)

        if (result != null) {
            return result
        }
        var remoteId: String? = selectedOption.value.remoteId

        println("loadCharacter: URL or ID")

        // user hand-entered a characterID / URL ... first check for validity
        remoteId = CmdTest.getCharacterId(urlOrId)
        if (remoteId == null) {
            return null
        }

        // ID appears valid; fetch from remote storage
        try {
            runBlocking {
                result = if (urlOrId.contains("http") && !urlOrId.contains("dndbeyond")) {
                    // content hosted somewhere other than dndbeyond
                    CmdTest.getRemoteCharacterByUrl(urlOrId)
                }
                else {
                    // default: dndbeyond
                    CmdTest.getRemoteCharacter(remoteId)
                }
            }
            if (result != null) {
                // on a good fetch, update local storage as well as the menu
                dprFiles.saveCharacter(result!!, remoteId)
            }
        } catch (e: Exception) {
            println("Error getting character, $e")
            //println("CharacterID invalid / not found")
            return null
        }
/*
        if (result != null) {
            println("loadCharacter: stat block before: $statBlock")
            statBlock.copyValues(character!!.getStatBlock())
            println("loadCharacter: stat block after: $statBlock")
            println(character!!.toStringWeapons()+"\n"+character!!.toStringFeats())
        }
  */
        return result
    }
}