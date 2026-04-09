package com.vikinghelmet.dnd.dpr.character

import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import junit.framework.TestCase.assertEquals
import kotlinx.serialization.json.Json
import java.io.InputStream
import kotlin.test.Test

class CharacterTest {

    val eldir = getCharacter("eldir")
    val kael = getCharacter("kael")
    val lars = getCharacter("lars")
    val leif = getCharacter("leif")
    val oleg = getCharacter("oleg")
    val rhogar = getCharacter("rhogar")
    val party = listOf(eldir,kael,lars,leif,oleg,rhogar)

    fun getResource(fileName: String): String? {
        val inputStream: InputStream = object {}.javaClass.getResourceAsStream("/$fileName") ?: return null
        try {
            return inputStream.bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun getCharacter(name: String): Character = Json.Default.decodeFromString(getResource("party/$name.json")!!)

    @Test
    fun getNameTest() {
        assertEquals("Eldir Ashenfell", eldir.getName())
        assertEquals("Kael Blackthorne", kael.getName())
        assertEquals("Lars Crafty", lars.getName())
        assertEquals("Leif Lightfoot", leif.getName())
        assertEquals("Oleg the shit faced", oleg.getName())
        assertEquals("Rhogar \"Stryker\" Flameborn", rhogar.getName())
    }

    @Test
    fun getLevelTest() {
        party.forEach { assertEquals(2, it.getLevel()) }
    }

    @Test
    fun is2014Test() {
        party.filter { it != kael }.forEach { assertEquals(false, it.is2014()) }
        assertEquals(true, kael.is2014())
    }

    fun abilityMap(list: List<Int>): Map<AbilityType,Int> {
        return AbilityType.entries.filter { it != AbilityType.ALL }.zip(list).toMap()
    }

    @Test
    fun getModifiedAbilityScoreMapTest() {
        assertEquals(abilityMap(listOf(12,15,16,19,12,12)), eldir.getModifiedAbilityScoreMap())
        assertEquals(abilityMap(listOf(10,14,10,18,16,14)), kael.getModifiedAbilityScoreMap())
        assertEquals(abilityMap(listOf(10,18,12,14,12,15)), lars.getModifiedAbilityScoreMap())
        assertEquals(abilityMap(listOf(12,19,14,8,14,8)),   leif.getModifiedAbilityScoreMap())
        assertEquals(abilityMap(listOf(18,13,17,13,8,13)),  oleg.getModifiedAbilityScoreMap())
        assertEquals(abilityMap(listOf(15,14,10,15,14,14)), rhogar.getModifiedAbilityScoreMap())
    }
}