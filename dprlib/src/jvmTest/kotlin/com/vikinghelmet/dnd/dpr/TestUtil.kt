package com.vikinghelmet.dnd.dpr

import com.vikinghelmet.dnd.dpr.character.PlayerCharacter
import com.vikinghelmet.dnd.dpr.editable.EditableFields
import com.vikinghelmet.dnd.dpr.editable.EditablePlayerCharacter
import com.vikinghelmet.dnd.dpr.scenario.onesided.ScenarioBuilder
import com.vikinghelmet.dnd.dpr.scenario.onesided.ScenarioCalculator
import com.vikinghelmet.dnd.dpr.scenario.onesided.ScenarioResult
import com.vikinghelmet.dnd.dpr.util.Constants.DEFAULT_TARGET_SPACING
import com.vikinghelmet.dnd.dpr.util.Globals
import dev.shivathapaa.logger.api.LogLevel
import dev.shivathapaa.logger.api.LoggerFactory
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import java.io.InputStream

object TestUtil {
    @Transient private val logger = LoggerFactory.get(TestUtil::class.simpleName ?: "")

    init {
        JulConfigurator()
        Globals.initLogger(LogLevel.WARN) // DEBUG

        for (filename in mutableListOf("spells.json","extra.spells.json")) {
            Globals.addSpells(getResource(filename) ?: "[]")
        }

        Globals.addSubclassSpellsPrepared(getResource("subclass.spellprep.json") ?: "[]")

        //Globals.addMonsters(getResource("monsters.json") ?: "[]")
        Globals.addMonsters(getResource("monsters.srd.json") ?: "[]")
    }

    fun dependency() {}

    fun getResource(fileName: String): String? {
        val inputStream: InputStream = object {}.javaClass.getResourceAsStream("/$fileName") ?: return null
        try {
            return inputStream.bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun getCharacter(filename: String): PlayerCharacter = Json.Default.decodeFromString(getResource(filename)!!)

    fun getEditableCharacter(filename: String): EditablePlayerCharacter {
        val editableFields: EditableFields = Json.Default.decodeFromString(getResource(filename)!!)
        return EditablePlayerCharacter(getCharacter(editableFields.remoteId), editableFields)
    }

    fun bestDPR (level: Int, character: EditablePlayerCharacter, numTargets: Int, range: Int): SimpleResult {
        character.editableFields.level = level
        val scenarioList = ScenarioBuilder(character, Globals.getMonster("Goblin"))
            .build(range, 5, numTargets, DEFAULT_TARGET_SPACING)

        //println("ScenarioList size: ${scenarioList.size}")
        logger.debug { "ScenarioList size: ${scenarioList.size}" }

        val scenarioResultList = scenarioList.map { ScenarioCalculator(it).calculateDPRForAllTurns() }.toList()

        //println("scenarioResultList size: ${scenarioResultList.size}")
        logger.debug { "scenarioResultList: size = ${ scenarioResultList.size }" }

        val topResult = ScenarioResult.topResults(scenarioResultList, 1)[0]
        return SimpleResult(topResult)
    }

    fun getExpectedResults(name: String): List<SimpleResult> {
        val json = TestUtil.getResource("expectedResults/$name")
        val expected: List<SimpleResult> = Json.Default.decodeFromString(json!!)

        expected.forEach { it ->
            if (it.clone != null) it.attacks = expected.first { it2 -> it2.level == it.clone }.attacks
        }
        return expected
    }

    val eldir = getCharacter("party2/eldir.json")
    val kael =  getCharacter("party2/kael.json")
    val lars =  getCharacter("party2/lars.json")
    val leif =  getCharacter("party2/leif.json")
    val oleg =  getCharacter("party2/oleg.json")
    val rhogar = getCharacter("party2/rhogar.json")
    val party = listOf(eldir, kael, lars, leif, oleg, rhogar)

    val eldir3 = getCharacter("party3/eldir.json")
    val kael3 =  getCharacter("party3/kael.json")
    val lars3 =  getCharacter("party3/lars.json")
    val leif3 =  getCharacter("party3/leif.json")
    val oleg3 =  getCharacter("party3/oleg.json")
    val rhogar3 = getCharacter("party3/rhogar.json")
    val party3 = listOf(eldir3, kael3, lars3, leif3, oleg3, rhogar3)

    val ww = getCharacter("ranger.subclass/ww.json")
    val gs = getCharacter("ranger.subclass/gs.json")
    val hunter = getCharacter("ranger.subclass/hunter.json")

    val leifPlan = getEditableCharacter("plan/leif.json")
    val wwPlan = getEditableCharacter("plan/ww.json")
    val wwCCPlan = getEditableCharacter("plan/wwCC.json")
    val gsPlan = getEditableCharacter("plan/gs.json")
    val gsDexPlan = getEditableCharacter("plan/gsDex.json")
    val hunterPlan = getEditableCharacter("plan/hunter.json")
}