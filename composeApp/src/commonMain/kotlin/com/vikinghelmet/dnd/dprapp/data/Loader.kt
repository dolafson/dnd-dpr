package com.vikinghelmet.dnd.dprapp.data

import com.vikinghelmet.dnd.dpr.character.Character
import com.vikinghelmet.dnd.dpr.editable.EditableCharacter
import com.vikinghelmet.dnd.dpr.editable.EditableFields
import com.vikinghelmet.dnd.dpr.util.CharacterAPI
import com.vikinghelmet.dnd.dpr.util.CharacterAPI.logger
import com.vikinghelmet.dnd.dprapp.ui.screens.dprFiles
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

object Loader {

    fun getEditableCharacter(name: String): EditableCharacter? {
        return dprFiles.getEditableCharacter(name)
    }

    fun getRemoteJson(urlOrId: String): String? {
        if (urlOrId.startsWith("/")) {   // read from file; this option is only feasible with jvmMain ...
            return dprFiles.read(urlOrId, false)
        }

        val url = CharacterAPI.getCharacterApiURL(urlOrId) ?: return null
        logger.info { "getRemoteJson, url: $url" }
        var json: String? = null
        runBlocking {
            json = CharacterAPI.getRequest(url)
        }
        return json
    }

    fun addEditableCharacter(json: String): EditableCharacter?
    {
        val baseline: Character = Json.Default.decodeFromString(json) ?: return null
        val result = EditableCharacter(baseline, EditableFields(baseline))

        // save raw json (large), so we have local access to ALL remote data, even if we don't know what to do with it yet
        dprFiles.saveCharacter(json, baseline.characterData.id.toString())

        // also save the (smaller) editable version, this drives the menu display
        dprFiles.saveEditableCharacter(result.editableFields)
        return result
    }
}