package com.vikinghelmet.dnd.dprapp.data

import com.vikinghelmet.dnd.dpr.CmdTest
import com.vikinghelmet.dnd.dpr.character.Character
import com.vikinghelmet.dnd.dpr.modified.EditableCharacter
import com.vikinghelmet.dnd.dpr.modified.EditableFields
import com.vikinghelmet.dnd.dpr.monsters.Monster
import com.vikinghelmet.dnd.dpr.util.DprSettings
import com.vikinghelmet.dnd.dpr.util.Globals
import com.vikinghelmet.dnd.dprapp.ui.dprFiles
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

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
            e.printStackTrace()
            return null
        }
    }

    fun getEditableCharacter(name: String): EditableCharacter? {
        return dprFiles.getEditableCharacter(name)
    }

    fun getCharacterFromSettings(settings: DprSettings): EditableCharacter? {
        println("Loader.getCharacter, settings name = ${settings.characterName}")
        if (settings.characterName.isEmpty()) return null
        return dprFiles.getEditableCharacter(settings.characterName)
    }

    fun addCharacter(urlOrId: String): EditableCharacter?
    {
        var result: EditableCharacter? = null
        var remoteId: String? = CmdTest.getCharacterId(urlOrId)

        println("addCharacter: urlOrId = $urlOrId")

        // user hand-entered a characterID / URL ... first check for validity
        if (remoteId == null) {
            return null
        }

        // ID appears valid; fetch from remote storage
        try {
            runBlocking {
                // build a new URL if needed
                val url = if (remoteId == urlOrId || urlOrId.contains("dndbeyond")) CmdTest.getCharacterApiURL(remoteId)
                            else urlOrId
                val json = CmdTest.getRequest(url)

                val baseline: Character = Json.decodeFromString(json)

                // on a good fetch, update local storage as well as the menu
                result = EditableCharacter(baseline, EditableFields.fromCharacter(baseline))

                // save raw json (large), so we have local access to ALL remote data, even if we don't know what to do with it yet
                dprFiles.saveCharacter(json, remoteId)

                // also save the (smaller) editable version, this drives the menu display
                dprFiles.saveEditableCharacter(result.editableFields)

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