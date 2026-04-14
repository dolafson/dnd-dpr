package com.vikinghelmet.dnd.dpr.util

import com.vikinghelmet.dnd.dpr.character.Character
import com.vikinghelmet.dnd.dpr.editable.EditableCharacter
import com.vikinghelmet.dnd.dpr.editable.EditableFields
import kotlinx.io.*
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.json.Json

const val settingsPath = ".dpr/settings.json"
const val characterBaselineDir = ".dpr/character/baseline"
const val characterEditableDir = ".dpr/character/editable"
const val exportDir = ".dpr/export"


class DprFiles(val appDataDir: String)
{
    fun init() {
        SystemFileSystem.createDirectories(Path(appDataDir + "/" + characterBaselineDir))
        SystemFileSystem.createDirectories(Path(appDataDir + "/" + characterEditableDir))
        SystemFileSystem.createDirectories(Path(appDataDir + "/" + exportDir))
    }

    fun deleteAll() {
        for (file in list(characterBaselineDir) + list(characterEditableDir)) {
            SystemFileSystem.delete(Path(file))
        }
        saveSettings(DprSettings())
    }

    fun getSettings(): DprSettings {
        val settings: DprSettings = Json.Default.decodeFromString(read(settingsPath) ?: "{}")
        println("settings: $settings")
        return settings
    }

    fun saveSettings(settings: DprSettings) {
        val settingsString = Json.Default.encodeToString(settings)
        write(settingsString, settingsPath)
    }

    fun saveCharacter(json: String, characterId: String) {
        write(json, characterBaselineDir +"/"+characterId)
    }

    fun saveEditableCharacter(editableFields: EditableFields) {
        write(Json.Default.encodeToString(editableFields), characterEditableDir +"/"+editableFields.name)
    }

    fun saveAttackCSV(csv: String) {
        write(csv, exportDir +"/attack.csv")
    }

    fun readAttackCSV(): String? {
        return read(exportDir +"/attack.csv")
    }

    fun getAttackCSVLocalUrl(): String {
        return "file://$appDataDir/${exportDir}/attack.csv"
    }
    fun deleteEditableCharacter(name: String) {
        delete(characterEditableDir +"/"+name)
    }

    fun getEditableFields(name: String): EditableFields? {
        if (name.isEmpty()) return null
        val json = read(characterEditableDir +"/" + name) ?: return null
        return Json.Default.decodeFromString(json)
    }

    fun getEditableCharacter(name: String): EditableCharacter? {
        //println("getEditableCharacter, settings name = $name")
        val editableFields = getEditableFields(name) ?: return null
        //println("getEditableCharacter, editableFields = $editableFields")
        if (editableFields.remoteId.isEmpty()) {
            println("getEditableCharacter, remoteId=0")
            return null
        }
        val baseline = getCharacter(editableFields.remoteId) ?: return null
        //println("getEditableCharacter, baseline = $baseline")

        val result = EditableCharacter(baseline, editableFields)
        if (baseline.getAlwaysPreparedSpells().isEmpty() && result.getAlwaysPreparedSpells().isNotEmpty()) {
            baseline.alwaysPrepared = result.getAlwaysPreparedSpells()
        }

        //println("on file load, always prepared: baseline = ${ baseline.getAlwaysPreparedSpells() }, editable = ${ result.getAlwaysPreparedSpells() }")
        return result
    }

    fun getCharacter(characterId: String): Character? {
        val json = read(characterBaselineDir +"/" + characterId)
        return if (json !=null) Json.Default.decodeFromString(json) else null
    }

    fun getEditableCharacterList(): List<String> {
        return list(characterEditableDir).map { p -> p.substringAfterLast('/') }
    }

    fun list(subdir: String): List<String> {
        val pathList = SystemFileSystem.list(Path(appDataDir + "/" + subdir))
        return pathList.map { p -> p.toString() }
    }

    fun write(data: String, filename: String) {
        try {
            SystemFileSystem.sink(Path(appDataDir + "/" + filename)).buffered().use { sink: Sink ->
                sink.writeString(data)
                sink.flush()
            }
            println("Data written to $filename")
        } catch (e: Exception) {
            println("Error writing file: $e")
        }
    }

    fun read(filename: String, prependAppDataDir: Boolean = true): String? {
        try {
            val path = if (prependAppDataDir) Path(appDataDir + "/" + filename) else Path(filename)

            SystemFileSystem.source(path).buffered().use { source: Source ->
                val content = source.readString()
                return content
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("Error reading file: $e")
            return null
            //return "{}" // TODO: better default ?
        }
    }

    fun delete(filename: String) {
        try {
            SystemFileSystem.delete(Path(appDataDir + "/" + filename))
        } catch (e: Exception) {
            e.printStackTrace()
            println("Error deleting file: $e")
        }
    }

}