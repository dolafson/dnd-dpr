package com.vikinghelmet.dnd.dpr.util

import com.vikinghelmet.dnd.dpr.character.spells.SpellsChanged2024
import com.vikinghelmet.dnd.dpr.monsters.Monster
import com.vikinghelmet.dnd.dpr.spells.Spell
import com.vikinghelmet.dnd.dpr.spells.SubclassSpellsPrepared
import dev.shivathapaa.logger.api.LogLevel
import dev.shivathapaa.logger.api.LoggerFactory
import dev.shivathapaa.logger.core.LoggerConfig
import dev.shivathapaa.logger.formatters.LogFormatters
import dev.shivathapaa.logger.sink.DefaultLogSink
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.math.round

object Globals {
    val spells = ArrayList<Spell>()
    val monsters = ArrayList<Monster>()
    val subclassSpellsPrepared  = ArrayList<SubclassSpellsPrepared>()

    @Serializable
    data class FeatureSet(
        val racialTraits: List<String>,
        val actionModifiers: List<String>,
        val feats: List<String>,
    )

    fun round2(float: Float): Float {
        return round(float * 100) / 100
    }
    fun getPercent(float: Float): String {
        return "${round2(float)}"
    }

    fun closeEnough(f1: Float, f2: Float): Boolean {
        return getPercent(f1) == getPercent(f2)
    }

    fun wrapWithQuotes(input: String): String {
        return StringBuilder().append("\"").append(input).append("\"").toString()
    }

    fun rightPad(input: String, pad: Int): String {
        val result = StringBuilder()
        result.append(input)
        while (result.length < pad) { result.append(" ") }
        return result.toString()
    }

    fun addSpells(jsonArrayAsString: String) {
        spells.addAll (Json.decodeFromString (jsonArrayAsString))
    }

    fun addMonsters(jsonArrayAsString: String) {
        monsters.addAll (Json.decodeFromString (jsonArrayAsString))
    }

    fun addSubclassSpellsPrepared(jsonArrayAsString: String) {
        subclassSpellsPrepared.addAll (Json.decodeFromString (jsonArrayAsString))
    }

    fun getSubclassSpellsPrepared(subclass: String): List<SubclassSpellsPrepared> {
        return subclassSpellsPrepared.filter { it.subclass == subclass }
    }

    fun getSpell(name: String, is2014: Boolean): Spell { //  character!!.is2014()
        val matches = spells.filter { it.name == name }

        if (!matches.isEmpty()) {
            val spellChanged = SpellsChanged2024.contains(name)
            val spellIn2024 = matches.firstOrNull { !it.is2014() }

            if (!spellChanged && (spellIn2024 != null)) {
                return spellIn2024
            }

            for (spell in matches) {
                if (spell.is2014And2024() || (is2014 == spell.is2014())) {
                    return spell
                }
            }
        }
        throw IllegalArgumentException("spell not found: "+name)
    }

    fun getSpellOrNull(name: String, is2014: Boolean): Spell? = try { getSpell(name, is2014) } catch (e: Exception) { null }

    fun getSpellsForClass(className: String, is2014: Boolean): List<Spell> {
        // make multiple passes thru the spell list
        // this is less efficient, but easier to handle the SpellsChanged2024 logic
        // TODO: optimize
        return spells.filter {
                spell -> (spell.properties.Classes ?: "").contains(className)
            }
            .map { it.name }
            .distinct()
            .mapNotNull { name ->
                try { getSpell(name, is2014) } catch (e: Exception) {
                    LoggerFactory.get(Globals::class.simpleName ?: "").error{
                        "getSpellsForClass: spell not found: $name"
                    }
                    null
                }
            }.toList()
    }

    fun getMonster(name: String): Monster {
        return getMonsterOrNull(name) ?: throw IllegalArgumentException("Monster not found: $name")
    }

    fun getMonsterOrNull(name: String): Monster? {
        return monsters.firstOrNull { it.monsterName == name }
    }


    fun addWStoCamelCase(input: String): String {
        // ensure capitalization of each word, and join with single space
        val words = input.split(Regex("[\\s_]+"))
        return words.joinToString(" ") { word -> word.lowercase().replaceFirstChar { it.uppercase() } }
    }

    fun removeNonAlpha(input: String): String {
        // replace dash with WS, then strip anything not alpha or WS, then ensure capitalization of each word, and join with empty string
        val words = input.replace("-"," ").replace("[^A-Za-z ]".toRegex(), "").split(Regex("[\\s_]+"))
        return words.joinToString("") { word -> word.lowercase().replaceFirstChar { it.uppercase() } }
    }

    // logging ...
    fun debug(str: String) {
        LoggerFactory.get(Globals::class.simpleName ?: "no simpleName").debug{str}
    }

    /*
Level	Priority	Usage
VERBOSE	0	Most detailed
DEBUG	1	Debugging information
INFO	2	General informational messages
WARN	3	Warning messages for potential issues
ERROR	4	Error messages for failures
FATAL	5	Critical errors (flushes sinks and crashes)
OFF	6	Disables all logging
     */
    fun initLogger(level: LogLevel) {
        // notes:
        //      ConsoleSink writes to /dev/stdout
        //      DefaultLogSink writes to /dev/stderr
        val config = LoggerConfig.Builder()
            .minLevel(level)
            //.minLevel(LogLevel.VERBOSE)
            //.override("MyApp", level)
            .addSink(
                DefaultLogSink(
                    logFormatter = LogFormatters.compact(showEmoji = false)
                    //logFormatter = LogFormatters.default(false)
                    //logFormatter = LogFormatters.pretty(false)
                    //logFormatter = LogFormatters.json(false)
                )
            )  // Choose predefined sink or create custom
            .build()

        LoggerFactory.install(config)

        LoggerFactory.get(Globals::class.simpleName ?: "").warn { "logging initialized" }
    }

    fun probableResult(valueIfSuccess: Float, valueIfFail: Float, chanceOfSuccess: Float): Float {
        return chanceOfSuccess * valueIfSuccess + (1-chanceOfSuccess)*valueIfFail
    }
}