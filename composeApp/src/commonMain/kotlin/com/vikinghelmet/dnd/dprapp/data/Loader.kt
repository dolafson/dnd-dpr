package com.vikinghelmet.dnd.dprapp.data

import com.vikinghelmet.dnd.dpr.CmdTest
import com.vikinghelmet.dnd.dpr.character.Character
import com.vikinghelmet.dnd.dpr.editable.EditableCharacter
import com.vikinghelmet.dnd.dpr.editable.EditableFields
import com.vikinghelmet.dnd.dpr.monsters.Monster
import com.vikinghelmet.dnd.dpr.util.Globals
import com.vikinghelmet.dnd.dprapp.ui.widgets.dprFiles
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

object Loader {

    fun getMonster(name: String): Monster? {
        try {
            return if (name.isEmpty()) null else Globals.getMonster(name)
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

    fun addEditableCharacter(urlOrId: String): EditableCharacter?
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
                val url = if (remoteId == urlOrId || urlOrId.contains("dndbeyond")) CmdTest.getCharacterApiURL(remoteId!!)
                            else urlOrId
                val json = CmdTest.getRequest(url)

                val baseline: Character = Json.decodeFromString(json)

                // on a good fetch, update local storage as well as the menu
                result = EditableCharacter(baseline, EditableFields(baseline))

                // update remoteId based on parsed data; this will be used as the local filename
                remoteId = baseline.characterData.id.toString()

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
        return result
    }

    fun loadParty(): List<EditableCharacter> {
        return listOf("eldir", "kael", "lars", "leif", "oleg", "rhogar",).mapNotNull {
            addEditableCharacter("https://www.vikinghelmet.com/dnd/party/$it.json")
        }
    }
}