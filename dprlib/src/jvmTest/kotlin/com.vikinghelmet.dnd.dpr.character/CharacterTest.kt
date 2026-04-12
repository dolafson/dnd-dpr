package com.vikinghelmet.dnd.dpr.character

import com.vikinghelmet.dnd.dpr.character.feats.Feat
import com.vikinghelmet.dnd.dpr.character.feats.FeatAdded
import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import junit.framework.TestCase.assertEquals
import kotlinx.serialization.json.Json
import java.io.InputStream
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CharacterTest {

    val eldir = getCharacter("eldir")
    val kael = getCharacter("kael")
    val lars = getCharacter("lars")
    val leif = getCharacter("leif")
    val oleg = getCharacter("oleg")
    val rhogar = getCharacter("rhogar")
    val party = listOf(eldir, kael, lars, leif, oleg, rhogar)

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

    fun abilityMap(list: List<Int>): Map<AbilityType, Int> {
        return AbilityType.getAllNotALL().zip(list).toMap()
    }

    @Test
    fun getRawAbilityScoreMapTest() {
        assertEquals(abilityMap(listOf(12, 15, 15, 17, 12, 12)), eldir.getRawAbilityScoreMap())
        assertEquals(abilityMap(listOf(10, 14, 10, 17, 16, 12)), kael.getRawAbilityScoreMap())
        assertEquals(abilityMap(listOf(10, 16, 12, 14, 11, 15)), lars.getRawAbilityScoreMap())
        assertEquals(abilityMap(listOf(12, 17, 14, 8, 13, 8)), leif.getRawAbilityScoreMap())
        assertEquals(abilityMap(listOf(16, 13, 16, 13, 8, 13)), oleg.getRawAbilityScoreMap())
        assertEquals(abilityMap(listOf(15, 14, 9, 13, 14, 14)), rhogar.getRawAbilityScoreMap())
    }

    @Test
    fun getModifiedAbilityScoreMapTest() {
        assertEquals(abilityMap(listOf(12, 15, 16, 19, 12, 12)), eldir.getModifiedAbilityScoreMap())
        assertEquals(abilityMap(listOf(10, 14, 10, 18, 16, 14)), kael.getModifiedAbilityScoreMap())
        assertEquals(abilityMap(listOf(10, 18, 12, 14, 12, 15)), lars.getModifiedAbilityScoreMap())
        assertEquals(abilityMap(listOf(12, 19, 14, 8, 14, 8)), leif.getModifiedAbilityScoreMap())
        assertEquals(abilityMap(listOf(18, 13, 17, 13, 8, 13)), oleg.getModifiedAbilityScoreMap())
        assertEquals(abilityMap(listOf(15, 14, 10, 15, 14, 14)), rhogar.getModifiedAbilityScoreMap())
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

        assertEquals(listOf(2, 0, 0, 0, 0, 0, 0, 0, 0), leif.getSpellSlots())
        assertEquals(listOf(3, 0, 0, 0, 0, 0, 0, 0, 0), kael.getSpellSlots())
        assertEquals(listOf(3, 0, 0, 0, 0, 0, 0, 0, 0), eldir.getSpellSlots())

        assertEquals(listOf(3, 0, 0, 0, 0, 0, 0, 0, 0), leif.getSpellSlotsIncludingExtraForPrepared(leif.getLevel()))
        assertEquals(listOf(3, 0, 0, 0, 0, 0, 0, 0, 0), kael.getSpellSlotsIncludingExtraForPrepared(kael.getLevel()))
        assertEquals(listOf(5, 0, 0, 0, 0, 0, 0, 0, 0), eldir.getSpellSlotsIncludingExtraForPrepared(eldir.getLevel()))
    }

    @Test
    fun getWeaponsTest() {
        assertEquals(listOf("Dagger", "Dagger", "Quarterstaff"), eldir.getWeaponList().map { it.name })
        assertEquals(listOf("Dagger", "Dagger", "Shortbow", "Shortsword"), lars.getWeaponList().map { it.name })
        assertEquals(listOf("Mace", "Crossbow"), kael.getWeaponList().map { it.name })
        assertEquals(listOf("Longsword", "Battleaxe", "Longbow"), rhogar.getWeaponList().map { it.name })

        assertEquals(
            listOf("Dagger", "Quarterstaff", "Shortsword", "Shortsword", "Longbow"),
            leif.getWeaponList().map { it.name })
        assertEquals(
            listOf("Handaxe", "Handaxe", "Handaxe", "Handaxe", "Greataxe", "Morningstar"),
            oleg.getWeaponList().map { it.name })

        // entire party has a single magic weapon
        assertEquals(1, party.flatMap { it.getWeaponList() }.count { it2 -> it2.magic })

        // the magic weapon gets +1 for attack and damage
        assertEquals(1, party.flatMap { it.getWeaponList() }.first { it2 -> it2.magic }.getMagicBonus())

        // total number of weapons
        assertEquals(23, party.flatMap { it.getWeaponList() }.count())

        // weapons with nicknames
        assertEquals(2, party.flatMap { it.getWeaponList() }.count { it2 -> it2.nickname != null })

        // count by damage type
        assertEquals(
            mapOf("1d4" to 5, "1d6" to 11, "1d8" to 6, "1d12" to 1),
            party.flatMap { it.getWeaponList() }
                .groupingBy { it2 -> it2.damage }.eachCount()
        )

        // count by range
        assertEquals(
            mapOf(5 to 10, 20 to 9, 80 to 2, 150 to 2),
            party.flatMap { it.getWeaponList() }
                .groupingBy { it2 -> it2.range }.eachCount()
        )

        // count by property
        assertEquals(
            mapOf(
            "Finesse" to 8, "Light" to 12, "Thrown" to 9, "Nick" to 5, "Versatile" to 4,
            "Topple" to 3, "Sap" to 3, "Ammunition" to 4, "Loading" to 1, "Range" to 4,
            "Two-Handed" to 5, "Slow" to 3, "Vex" to 8, "Heavy" to 3, "Cleave" to 1
        ),
            party.map { it.getWeaponList() }
                .flatMap { s1 -> s1.map { it.getPropertyNames() } }
                .flatMap { s2 -> s2!!.toList() }
                .groupingBy { it }.eachCount()
        )
    }

    @Test
    fun getFeatAddedList() {
        // party.forEach { println("${it.getName()} \t ${it.getFeatAddedList()}") }

        listOf(kael, oleg).forEach { assertEquals(emptyList<FeatAdded>(), kael.getFeatAddedList()) }

        // these feats are not yet supported by our enumerations
        assertEquals(
            listOf("Magic Initiate (Wizard)", "Spellfire Spark"),
            eldir.getFeatAddedList().map { it.definition.name })
        assertEquals(listOf("Spellfire Spark"), lars.getFeatAddedList().map { it.definition.name })

        // these feats are well-known
        assertEquals(listOf(Feat.Archery), leif.getFeatAddedList().map { it.getFeat() })
        assertEquals(listOf(Feat.TwoWeaponFighting), rhogar.getFeatAddedList().map { it.getFeat() })
    }

    @Test
    fun getActionList() {
        assertEquals(listOf("Magical Absorption", "Arcane Recovery"),
            eldir.getActionList().map { it.name })

        assertEquals(listOf("Channel Divinity", "Channel Divinity: Turn Undead", "Channel Divinity: Preserve Life", "Harness Divine Power"),
            kael.getActionList().map { it.name })

        assertEquals(listOf("Magical Absorption", "Vex (Shortbow)", "Vex (Shortsword)", "Sneak Attack"),
            lars.getActionList().map { it.name })

        assertEquals(listOf("Vex (Shortsword)", "Slow (Longbow)", "Hunter’s Mark"),
            leif.getActionList().map { it.name })

        assertEquals(listOf("Relentless Endurance", "Graze (Greatsword)", "Cleave (Greataxe)", "Rage (Enter)"),
            oleg.getActionList().map { it.name })

        assertEquals(listOf("Breath Weapon (Fire)", "Topple (Battleaxe)", "Sap (Longsword)",
                            "Slow (Longbow)", "Second Wind", "Action Surge", "Tactical Mind"),
            rhogar.getActionList().map { it.name })
    }

    @Test
    fun getRacialTraitList() {
        // party.forEach { println("${it.getName()} \t ${it.getRacialTraitList().map { it2 -> it2.definition.name }}") }

        party.forEach {
            //println("${it.getName()} \t ${it.getRacialTraitNameList()}")
            assertTrue (it.getRacialTraitNameList().containsAll(listOf("Size", "Speed", "Languages")))
        }
        listOf(eldir,lars,leif,rhogar).forEach {
            assertTrue (it.getRacialTraitNameList().containsAll(listOf("Creature Type","Ability Score Increases")))
        }
        listOf(kael, oleg).forEach {
            assertTrue (it.getRacialTraitNameList().contains("Ability Score Increase")) // not plural
        }
        listOf(kael, leif, oleg, rhogar).forEach {
            assertTrue (it.getRacialTraitNameList().contains("Darkvision"))
        }
        listOf(eldir,lars).forEach {
            assertTrue (eldir.getRacialTraitNameList().containsAll(listOf("Resourceful", "Skillful", "Versatile")))
        }
        assertTrue (kael.getRacialTraitNameList().containsAll(listOf("Hellish Resistance", "Infernal Legacy")))
        assertTrue (leif.getRacialTraitNameList().containsAll(listOf("Elven Lineage", "Fey Ancestry", "Keen Senses", "Trance", "Elven Lineage Spells")))
        assertTrue (oleg.getRacialTraitNameList().containsAll(listOf("Menacing", "Relentless Endurance", "Savage Attacks")))
        assertTrue (rhogar.getRacialTraitNameList().containsAll(listOf("Draconic Ancestry", "Breath Weapon", "Damage Resistance", "Draconic Flight")))
    }

    /*

        buf.append("classFeatures    = ${ getClassFeatureNames() }").append("\n")
        buf.append("subclassFeatures = ${ getSubclassFeatureNames() }").append("\n")

        buf.append("feats by level = ${ getClassFeaturesByLevel() }").append("\n")
        buf.append("levels for ASI = ${ getLevelsForAbilityIncrease() }").append("\n")
        buf.append("className      = ${ getClassName() }").append("\n")
        buf.append("spellsForClass = ${ getSpellsForClass() }").append("\n")
     */

    fun getClassFeaturesExceptFirstLevelAndASI(c: Character) =
        c.getClassFeaturesByLevel().filter { it.value > 1 && !it.key.contains("Ability Score")}

    @Test
    fun getClassFeatures() {
        party.forEach {
            println(it.getName())
            getClassFeaturesExceptFirstLevelAndASI(it).forEach { string, i ->  println("\t $i: $string") }
        }

        party.forEach {
            assertTrue (it.getClassFeatureNames().contains("Ability Score Improvement"))
        }
        listOf(eldir,lars,leif,oleg,rhogar).forEach {
            assertTrue (it.getClassFeatureNames().containsAll(listOf("8: Ability Score Improvement","12: Ability Score Improvement","16: Ability Score Improvement")))
        }
        // rhogar (fighter) gets 2 more ASI bumps than everyone else
        assertTrue (rhogar.getClassFeatureNames().containsAll(listOf("6: Ability Score Improvement","14: Ability Score Improvement")))

        listOf(eldir,kael,leif).forEach {
            assertTrue (it.getClassFeatureNames().contains("Spellcasting"))
        }

        assertEquals(
            mapOf(
                "Scholar" to 2,
                "Wizard Subclass" to 3,
                "Memorize Spell" to 5,
                "Spell Mastery" to 18,
                "Epic Boon" to 19,
                "Signature Spells" to 20,
            ),
            getClassFeaturesExceptFirstLevelAndASI(eldir))

        assertEquals(
            mapOf(
                "Channel Divinity" to 2,
                "Destroy Undead" to 5,
                "Divine Intervention" to 10,
            ),
            getClassFeaturesExceptFirstLevelAndASI(kael))
    }
}