package com.vikinghelmet.dnd.dpr

import com.vikinghelmet.dnd.dpr.character.Character
import com.vikinghelmet.dnd.dpr.util.Globals
import kotlinx.serialization.json.Json
import java.io.InputStream

object TestUtil {

    fun getResource(fileName: String): String? {
        val inputStream: InputStream = object {}.javaClass.getResourceAsStream("/$fileName") ?: return null
        try {
            return inputStream.bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun getCharacter(filename: String): Character = Json.Default.decodeFromString(getResource(filename)!!)

    init {
        for (filename in mutableListOf("spells.json","extra.spells.json")) {
            Globals.addSpells(getResource(filename) ?: "[]")
        }

        Globals.addSubclassSpellsPrepared(getResource("subclass.spellprep.json") ?: "[]")

        Globals.addMonsters(getResource("monsters.json") ?: "[]")
    }

    fun dependency() {}

    val eldir = getCharacter("party/eldir.json")
    val kael =  getCharacter("party/kael.json")
    val lars =  getCharacter("party/lars.json")
    val leif =  getCharacter("party/leif.json")
    val oleg =  getCharacter("party/oleg.json")
    val rhogar = getCharacter("party/rhogar.json")
    val party = listOf(eldir, kael, lars, leif, oleg, rhogar)

    val ww = getCharacter("ranger.subclass/leif.ww.json")
    val gs = getCharacter("ranger.subclass/leif.gs.json")
    val hunter = getCharacter("ranger.subclass/leif.hunter.json")
}