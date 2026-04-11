package com.vikinghelmet.dnd.dpr.character

import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import junit.framework.TestCase.assertEquals
import kotlinx.serialization.json.Json
import java.io.InputStream
import kotlin.test.Test
import kotlin.test.assertNull

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
        return AbilityType.getAllNotALL().zip(list).toMap()
    }

    @Test
    fun getRawAbilityScoreMapTest() {
        assertEquals(abilityMap(listOf(12,15,15,17,12,12)), eldir.getRawAbilityScoreMap())
        assertEquals(abilityMap(listOf(10,14,10,17,16,12)), kael.getRawAbilityScoreMap())
        assertEquals(abilityMap(listOf(10,16,12,14,11,15)), lars.getRawAbilityScoreMap())
        assertEquals(abilityMap(listOf(12,17,14,8,13,8)),   leif.getRawAbilityScoreMap())
        assertEquals(abilityMap(listOf(16,13,16,13,8,13)),  oleg.getRawAbilityScoreMap())
        assertEquals(abilityMap(listOf(15,14,9,13,14,14)), rhogar.getRawAbilityScoreMap())
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

    @Test
    fun spellCastingTest() {
        listOf(lars, oleg, rhogar).forEach { assertNull(it.getSpellAbilityType()) }

        assertEquals(AbilityType.Wisdom, leif.getSpellAbilityType())
        assertEquals(AbilityType.Wisdom, kael.getSpellAbilityType())
        assertEquals(AbilityType.Intelligence, eldir.getSpellAbilityType())

        assertEquals(4, leif.getSpellBonusToHit())
        assertEquals(5, kael.getSpellBonusToHit())
        assertEquals(6, eldir.getSpellBonusToHit())

        assertEquals(12, leif.getSpellSaveDC())
        assertEquals(13, kael.getSpellSaveDC())
        assertEquals(14, eldir.getSpellSaveDC())
    }

    @Test
    fun getWeaponsTest() {
        assertEquals(listOf("Dagger","Dagger","Quarterstaff"),          eldir.getWeaponList().map { it.name })
        assertEquals(listOf("Dagger","Dagger","Shortbow","Shortsword"), lars.getWeaponList().map { it.name })
        assertEquals(listOf("Mace","Crossbow"),                         kael.getWeaponList().map { it.name })
        assertEquals(listOf("Longsword","Battleaxe","Longbow"),         rhogar.getWeaponList().map { it.name })

        assertEquals(listOf("Dagger","Quarterstaff","Shortsword","Shortsword","Longbow"),       leif.getWeaponList().map { it.name })
        assertEquals(listOf("Handaxe","Handaxe","Handaxe","Handaxe","Greataxe","Morningstar"), oleg.getWeaponList().map { it.name })

        // entire party has a single magic weapon
        assertEquals(1, party.flatMap { it.getWeaponList() }.count { it2 -> it2.magic })

        // the magic weapon gets +1 for attack and damage
        assertEquals(1, party.flatMap { it.getWeaponList() }.first { it2 -> it2.magic }.getMagicBonus())

        // total number of weapons
        assertEquals(23, party.flatMap { it.getWeaponList() }.count())

        // weapons with nicknames
        assertEquals(2, party.flatMap { it.getWeaponList() }.count { it2 -> it2.nickname != null })

        // count by damage type
        assertEquals(mapOf("1d4" to 5, "1d6" to 11, "1d8" to 6, "1d12" to 1),
            party.flatMap { it.getWeaponList() }
                .groupingBy { it2 -> it2.damage }.eachCount())

        // count by range
        assertEquals(mapOf(5 to 10, 20 to 9, 80 to 2, 150 to 2),
            party.flatMap { it.getWeaponList() }
                .groupingBy { it2 -> it2.range }.eachCount())

        // count by property
        assertEquals (mapOf ("Finesse" to 8, "Light" to 12, "Thrown" to 9, "Nick" to 5, "Versatile" to 4,
                        "Topple" to 3, "Sap" to 3, "Ammunition" to 4, "Loading" to 1, "Range" to 4,
                        "Two-Handed" to 5, "Slow" to 3, "Vex" to 8, "Heavy" to 3, "Cleave" to 1),
                party.map { it.getWeaponList() }
                    .flatMap { s1 -> s1.map { it.getPropertyNames() } }
                    .flatMap { s2 -> s2!!.toList() }
                    .groupingBy { it }.eachCount()
            )
    }
}