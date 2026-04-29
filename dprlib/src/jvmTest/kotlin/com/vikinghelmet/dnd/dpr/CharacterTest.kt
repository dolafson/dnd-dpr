package com.vikinghelmet.dnd.dpr

import com.vikinghelmet.dnd.dpr.character.Character
import com.vikinghelmet.dnd.dpr.character.feats.Feat
import com.vikinghelmet.dnd.dpr.character.feats.FeatAdded
import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CharacterTest {
    @Test
    fun getNameTest() {
        assertEquals("Eldir Ashenfell", TestUtil.eldir.getName())
        assertEquals("Kael Blackthorne", TestUtil.kael.getName())
        assertEquals("Lars Crafty", TestUtil.lars.getName())
        assertEquals("Leif Lightfoot", TestUtil.leif.getName())
        assertEquals("Oleg the shit faced", TestUtil.oleg.getName())
        assertEquals("Rhogar \"Stryker\" Flameborn", TestUtil.rhogar.getName())
    }

    @Test
    fun getLevelTest() {
        TestUtil.party.forEach { assertEquals(2, it.getLevel()) }
    }

    @Test
    fun is2014Test() {
        TestUtil.party.filter { it != TestUtil.kael }.forEach { assertEquals(false, it.is2014()) }
        assertEquals(true, TestUtil.kael.is2014())
    }

    fun abilityMap(list: List<Int>): Map<AbilityType, Int> {
        return AbilityType.getAllNotALL().zip(list).toMap()
    }

    @Test
    fun getRawAbilityScoreMapTest() {
        assertEquals(abilityMap(listOf(12, 15, 15, 17, 12, 12)), TestUtil.eldir.getRawAbilityScoreMap())
        assertEquals(abilityMap(listOf(10, 14, 10, 17, 16, 12)), TestUtil.kael.getRawAbilityScoreMap())
        assertEquals(abilityMap(listOf(10, 16, 12, 14, 11, 15)), TestUtil.lars.getRawAbilityScoreMap())
        assertEquals(abilityMap(listOf(12, 17, 14, 8, 13, 8)), TestUtil.leif.getRawAbilityScoreMap())
        assertEquals(abilityMap(listOf(16, 13, 16, 13, 8, 13)), TestUtil.oleg.getRawAbilityScoreMap())
        assertEquals(abilityMap(listOf(15, 14, 9, 13, 14, 14)), TestUtil.rhogar.getRawAbilityScoreMap())
    }

    @Test
    fun getModifiedAbilityScoreMapTest() {
        assertEquals(abilityMap(listOf(12, 15, 16, 19, 12, 12)), TestUtil.eldir.getModifiedAbilityScoreMap())
        assertEquals(abilityMap(listOf(10, 14, 10, 18, 16, 14)), TestUtil.kael.getModifiedAbilityScoreMap())
        assertEquals(abilityMap(listOf(10, 18, 12, 14, 12, 15)), TestUtil.lars.getModifiedAbilityScoreMap())
        assertEquals(abilityMap(listOf(12, 19, 14, 8, 14, 8)), TestUtil.leif.getModifiedAbilityScoreMap())
        assertEquals(abilityMap(listOf(18, 13, 17, 13, 8, 13)), TestUtil.oleg.getModifiedAbilityScoreMap())
        assertEquals(abilityMap(listOf(15, 14, 10, 15, 14, 14)), TestUtil.rhogar.getModifiedAbilityScoreMap())
    }

    @Test
    fun spellCastingTest() {
        listOf(TestUtil.lars, TestUtil.oleg, TestUtil.rhogar).forEach { assertNull(it.getSpellAbilityType()) }

        assertEquals(AbilityType.Wisdom, TestUtil.leif.getSpellAbilityType())
        assertEquals(AbilityType.Wisdom, TestUtil.kael.getSpellAbilityType())
        assertEquals(AbilityType.Intelligence, TestUtil.eldir.getSpellAbilityType())

        assertEquals(4, TestUtil.leif.getSpellBonusToHit())
        assertEquals(5, TestUtil.kael.getSpellBonusToHit())
        assertEquals(6, TestUtil.eldir.getSpellBonusToHit())

        assertEquals(12, TestUtil.leif.getSpellSaveDC())
        assertEquals(13, TestUtil.kael.getSpellSaveDC())
        assertEquals(14, TestUtil.eldir.getSpellSaveDC())

        assertEquals(listOf(2, 0, 0, 0, 0, 0, 0, 0, 0), TestUtil.leif.getSpellSlots())
        assertEquals(listOf(3, 0, 0, 0, 0, 0, 0, 0, 0), TestUtil.kael.getSpellSlots())
        assertEquals(listOf(3, 0, 0, 0, 0, 0, 0, 0, 0), TestUtil.eldir.getSpellSlots())

        assertEquals(listOf(3, 0, 0, 0, 0, 0, 0, 0, 0), TestUtil.leif.getSpellSlotsIncludingExtraForPrepared(TestUtil.leif.getLevel()))
        assertEquals(listOf(3, 0, 0, 0, 0, 0, 0, 0, 0), TestUtil.kael.getSpellSlotsIncludingExtraForPrepared(TestUtil.kael.getLevel()))
        assertEquals(listOf(5, 0, 0, 0, 0, 0, 0, 0, 0), TestUtil.eldir.getSpellSlotsIncludingExtraForPrepared(TestUtil.eldir.getLevel()))
    }

    @Test
    fun getWeaponsTest() {
        assertEquals(listOf("Dagger", "Dagger", "Quarterstaff"), TestUtil.eldir.getWeaponList().map { it.name })
        assertEquals(listOf("Dagger", "Dagger", "Shortbow", "Shortsword"), TestUtil.lars.getWeaponList().map { it.name })
        assertEquals(listOf("Mace", "Crossbow"), TestUtil.kael.getWeaponList().map { it.name })
        assertEquals(listOf("Longsword", "Battleaxe", "Longbow"), TestUtil.rhogar.getWeaponList().map { it.name })

        assertEquals(
            listOf("Dagger", "Quarterstaff", "Shortsword", "Shortsword", "Longbow"),
            TestUtil.leif.getWeaponList().map { it.name })
        assertEquals(
            listOf("Handaxe", "Handaxe", "Handaxe", "Handaxe", "Greataxe", "Morningstar"),
            TestUtil.oleg.getWeaponList().map { it.name })

        // entire party has a single magic weapon
        assertEquals(1, TestUtil.party.flatMap { it.getWeaponList() }.count { it2 -> it2.magic })

        // the magic weapon gets +1 for attack and damage
        assertEquals(1, TestUtil.party.flatMap { it.getWeaponList() }.first { it2 -> it2.magic }.getMagicBonus())

        // total number of weapons
        assertEquals(23, TestUtil.party.flatMap { it.getWeaponList() }.count())

        // weapons with nicknames
        assertEquals(2, TestUtil.party.flatMap { it.getWeaponList() }.count { it2 -> it2.nickname != null })

        // count by damage type
        assertEquals(
            mapOf("1d4" to 5, "1d6" to 11, "1d8" to 6, "1d12" to 1),
            TestUtil.party.flatMap { it.getWeaponList() }
                .groupingBy { it2 -> it2.damage }.eachCount()
        )

        // count by range
        assertEquals(
            mapOf(5 to 10, 20 to 9, 80 to 2, 150 to 2),
            TestUtil.party.flatMap { it.getWeaponList() }
                .groupingBy { it2 -> it2.range }.eachCount()
        )

        // count by property
        assertEquals(
            mapOf(
            "Finesse" to 8, "Light" to 12, "Thrown" to 9, "Nick" to 5, "Versatile" to 4,
            "Topple" to 3, "Sap" to 3, "Ammunition" to 4, "Loading" to 1, "Range" to 4,
            "Two-Handed" to 5, "Slow" to 3, "Vex" to 8, "Heavy" to 3, "Cleave" to 1
        ),
            TestUtil.party.map { it.getWeaponList() }
                .flatMap { s1 -> s1.map { it.getPropertyNames() } }
                .flatMap { s2 -> s2!!.toList() }
                .groupingBy { it }.eachCount()
        )
    }

    @Test
    fun getFeatAddedList() {
        // party.forEach { println("${it.getName()} \t ${it.getFeatAddedList()}") }

        listOf(TestUtil.kael, TestUtil.oleg).forEach { assertEquals(emptyList<FeatAdded>(), TestUtil.kael.getFeatAddedList()) }

        // these feats are not yet supported by our enumerations
        assertEquals(
            listOf("Magic Initiate (Wizard)", "Spellfire Spark"),
            TestUtil.eldir.getFeatAddedList().map { it.definition.name })
        assertEquals(listOf("Spellfire Spark"), TestUtil.lars.getFeatAddedList().map { it.definition.name })

        // these feats are well-known
        assertEquals(listOf(Feat.Archery), TestUtil.leif.getFeatAddedList().map { it.getFeat() })
        assertEquals(listOf(Feat.TwoWeaponFighting), TestUtil.rhogar.getFeatAddedList().map { it.getFeat() })
    }

    @Test
    fun getActionList() {
        assertEquals(listOf("Magical Absorption", "Arcane Recovery"),
            TestUtil.eldir.getActionList().map { it.name })

        assertEquals(listOf("Channel Divinity", "Channel Divinity: Turn Undead", "Channel Divinity: Preserve Life", "Harness Divine Power"),
            TestUtil.kael.getActionList().map { it.name })

        assertEquals(listOf("Magical Absorption", "Vex (Shortbow)", "Vex (Shortsword)", "Sneak Attack"),
            TestUtil.lars.getActionList().map { it.name })

        assertEquals(listOf("Vex (Shortsword)", "Slow (Longbow)", "Hunter’s Mark"),
            TestUtil.leif.getActionList().map { it.name })

        assertEquals(listOf("Relentless Endurance", "Graze (Greatsword)", "Cleave (Greataxe)", "Rage (Enter)"),
            TestUtil.oleg.getActionList().map { it.name })

        assertEquals(listOf("Breath Weapon (Fire)", "Topple (Battleaxe)", "Sap (Longsword)",
                            "Slow (Longbow)", "Second Wind", "Action Surge", "Tactical Mind"),
            TestUtil.rhogar.getActionList().map { it.name })
    }

    @Test
    fun getRacialTraitList() {
        TestUtil.party.forEach {
            assertTrue (it.getRacialTraitNameList().containsAll(listOf("Size", "Speed", "Languages")))
        }
        listOf(TestUtil.eldir, TestUtil.lars, TestUtil.leif, TestUtil.rhogar).forEach {
            assertTrue (it.getRacialTraitNameList().containsAll(listOf("Creature Type","Ability Score Increases")))
        }
        listOf(TestUtil.kael, TestUtil.oleg).forEach {
            assertTrue (it.getRacialTraitNameList().contains("Ability Score Increase")) // not plural
        }
        listOf(TestUtil.kael, TestUtil.leif, TestUtil.oleg, TestUtil.rhogar).forEach {
            assertTrue (it.getRacialTraitNameList().contains("Darkvision"))
        }
        listOf(TestUtil.eldir, TestUtil.lars).forEach {
            assertTrue (TestUtil.eldir.getRacialTraitNameList().containsAll(listOf("Resourceful", "Skillful", "Versatile")))
        }
        assertTrue (TestUtil.kael.getRacialTraitNameList().containsAll(listOf("Hellish Resistance", "Infernal Legacy")))
        assertTrue (TestUtil.leif.getRacialTraitNameList().containsAll(listOf("Elven Lineage", "Fey Ancestry", "Keen Senses", "Trance", "Elven Lineage Spells")))
        assertTrue (TestUtil.oleg.getRacialTraitNameList().containsAll(listOf("Menacing", "Relentless Endurance", "Savage Attacks")))
        assertTrue (TestUtil.rhogar.getRacialTraitNameList().containsAll(listOf("Draconic Ancestry", "Breath Weapon", "Damage Resistance", "Draconic Flight")))
    }

    fun getClassFeaturesExceptFirstLevelAndASI(c: Character) =
        c.getClassFeaturesByLevel().filter { it.value > 1 && !it.key.contains("Ability Score")}

    @Test
    fun getClassFeatures() {
        TestUtil.party.forEach {
            println(it.getName())
            getClassFeaturesExceptFirstLevelAndASI(it).forEach { string, i ->  println("\t $i: $string") }
        }

        TestUtil.party.forEach {
            assertTrue (it.getClassFeatureNames().contains("Ability Score Improvement"))
        }
        listOf(TestUtil.eldir, TestUtil.lars, TestUtil.leif, TestUtil.oleg, TestUtil.rhogar).forEach {
            assertTrue (it.getClassFeatureNames().containsAll(listOf("8: Ability Score Improvement","12: Ability Score Improvement","16: Ability Score Improvement")))
        }
        // rhogar (fighter) gets 2 more ASI bumps than everyone else
        assertTrue (TestUtil.rhogar.getClassFeatureNames().containsAll(listOf("6: Ability Score Improvement","14: Ability Score Improvement")))

        listOf(TestUtil.eldir, TestUtil.kael, TestUtil.leif).forEach {
            assertTrue (it.getClassFeatureNames().contains("Spellcasting"))
        }

        assertEquals(mapOf(
                "Scholar" to 2,
                "Wizard Subclass" to 3,
                "Memorize Spell" to 5,
                "Spell Mastery" to 18,
                "Epic Boon" to 19,
                "Signature Spells" to 20,
            ),
            getClassFeaturesExceptFirstLevelAndASI(TestUtil.eldir))

        assertEquals(mapOf(
                "Channel Divinity" to 2,
                "Destroy Undead" to 5,
                "Divine Intervention" to 10,
            ),
            getClassFeaturesExceptFirstLevelAndASI(TestUtil.kael))

        assertEquals(mapOf(
                "Cunning Action" 	 to 2,
                "Rogue Subclass" 	 to 3,
                "Steady Aim" 	 to 3,
                "Cunning Strike" 	 to 5,
                "Uncanny Dodge" 	 to 5,
                "6: Expertise" 	 to 6,
                "Evasion" 	 to 7,
                "Reliable Talent" 	 to 7,
                "Improved Cunning Strike" 	 to 11,
                "Devious Strikes" 	 to 14,
                "Slippery Mind" 	 to 15,
                "Elusive" 	 to 18,
                "Epic Boon" 	 to 19,
                "Stroke of Luck" 	 to 20,
            ),
            getClassFeaturesExceptFirstLevelAndASI(TestUtil.lars))

        assertEquals(mapOf(
                "Deft Explorer" 	 to 2,
                "Fighting Style" 	 to 2,
                "Ranger Subclass" 	 to 3,
                "Extra Attack" 	 to 5,
                "Roving" 	 to 6,
                "Expertise" 	 to 9,
                "Tireless" 	 to 10,
                "Relentless Hunter" 	 to 13,
                "Nature’s Veil" 	 to 14,
                "Precise Hunter" 	 to 17,
                "Feral Senses" 	 to 18,
                "Epic Boon" 	 to 19,
                "Foe Slayer" 	 to 20,
            ),
            getClassFeaturesExceptFirstLevelAndASI(TestUtil.leif))

        assertEquals(mapOf(
                "Danger Sense" 	 to 2,
                "Reckless Attack" 	 to 2,
                "Barbarian Subclass" 	 to 3,
                "Primal Knowledge" 	 to 3,
                "4: Weapon Mastery" 	 to 4,
                "Extra Attack" 	 to 5,
                "Fast Movement" 	 to 5,
                "Feral Instinct" 	 to 7,
                "Instinctive Pounce" 	 to 7,
                "Brutal Strike" 	 to 9,
                "10: Weapon Mastery" 	 to 10,
                "Relentless Rage" 	 to 11,
                "Improved Brutal Strike" 	 to 17,
                "Persistent Rage" 	 to 15,
                "Indomitable Might" 	 to 18,
                "Epic Boon" 	 to 19,
                "Primal Champion" 	 to 20,
            ),
            getClassFeaturesExceptFirstLevelAndASI(TestUtil.oleg))

        assertEquals(mapOf(
                "Action Surge" 	 to 2,
                "Tactical Mind" 	 to 2,
                "Fighter Subclass" 	 to 3,
                "4: Weapon Mastery" 	 to 4,
                "Extra Attack" 	 to 5,
                "Tactical Shift" 	 to 5,
                "Indomitable" 	 to 9,
                "Tactical Master" 	 to 9,
                "10: Weapon Mastery" 	 to 10,
                "Two Extra Attacks" 	 to 11,
                "Studied Attacks" 	 to 13,
                "16: Weapon Mastery" 	 to 16,
                "Epic Boon" 	 to 19,
                "Three Extra Attacks" 	 to 20,
            ),
            getClassFeaturesExceptFirstLevelAndASI(TestUtil.rhogar))
    }


    @Test
    fun getSpellsForClass() {
        //TestUtil.init()
        listOf(TestUtil.lars, TestUtil.oleg, TestUtil.rhogar).forEach { assertEquals(0, it.getSpellsForClass().size)}

        assertTrue(49  <= TestUtil.leif.getSpellsForClass().size)
        assertTrue(109 <= TestUtil.kael.getSpellsForClass().size)
        assertTrue(216 <= TestUtil.eldir.getSpellsForClass().size)
    }
}