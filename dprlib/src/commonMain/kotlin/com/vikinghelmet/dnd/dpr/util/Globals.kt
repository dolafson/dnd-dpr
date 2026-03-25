package com.vikinghelmet.dnd.dpr.util

import com.vikinghelmet.dnd.dpr.character.Character
import com.vikinghelmet.dnd.dpr.character.actions.ActionModifier
import com.vikinghelmet.dnd.dpr.character.feats.Feat
import com.vikinghelmet.dnd.dpr.character.race.RacialTrait
import com.vikinghelmet.dnd.dpr.monsters.Monster
import com.vikinghelmet.dnd.dpr.spells.Spell
import com.vikinghelmet.dnd.dpr.turn.Turn
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

    @Serializable
    data class FeatureSet(
        val racialTraits: List<String>,
        val actionModifiers: List<String>,
        val feats: List<String>,
    )

    fun getPercent(float: Float): String {
        val rounded = round(float * 100) / 100
        return "${rounded}"
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

    fun leftPad(input: String, pad: Int): String {
        val result = StringBuilder()
        repeat(pad - input.length) { result.append(" ") }
        result.append(input)
        return result.toString()
    }


    fun dumpFeatures() {
        println(Json.encodeToString(FeatureSet(
            RacialTrait.entries.map { e -> e.name },
            ActionModifier.entries.map { e -> e.name },
            Feat.entries.map { e -> e.name }
        )))
    }

    fun dump(arg: String, character: Character?, turns: List<Turn>) {
        if (!arg.contains(":")) {
            for (item in spells)    println(Json.encodeToString(item))
            for (item in monsters)  println(Json.encodeToString(item))
            for (item in turns)     println(Json.encodeToString(item))
            character?.dump()
            dumpFeatures()
            return
        }

        val dumpType = arg.split(":")[1]
        when (dumpType) {
            "spells" -> {
                for (item in spells)  println(Json.encodeToString(item))
            }
            "monsters" -> {
                for (item in monsters)  println(Json.encodeToString(item))
            }
            "attacks" -> {
                for (item in turns)  println(Json.encodeToString(item))
            }
            "features" -> {
                dumpFeatures()
            }
            "character" -> {
                debug("dump:character")
                character?.dump()
            }
        }
    }

    fun search(arg: String) {
        val searchValue = arg.split(":")[1]
        for (item in spells) if (item.name.contains(searchValue))  println(Json.encodeToString(item))
        for (item in monsters) if (item.name.contains(searchValue))  println(Json.encodeToString(item))
    }

    fun addSpells(jsonArrayAsString: String) {
        spells.addAll (Json.decodeFromString (jsonArrayAsString))
    }

    fun addMonsters(jsonArrayAsString: String) {
        monsters.addAll (Json.decodeFromString (jsonArrayAsString))
    }

    fun getSpell(name: String, is2014: Boolean): Spell { //  character!!.is2014()
        for (spell in spells) {
            if (spell.name == name && (spell.isSameIn2014And2024() || (is2014 == spell.is2014()))) return spell
        }
        throw IllegalArgumentException("spell not found: "+name)
    }

    fun getSpellOrNull(name: String, is2014: Boolean): Spell? = try { getSpell(name, is2014) } catch (e: Exception) { null }

    fun getSpellsForClass(className: String, is2014: Boolean): List<Spell> {
        return spells.filter { spell ->
            (spell.properties.Classes ?: "").contains(className) &&
                (spell.isSameIn2014And2024() || (is2014 == spell.is2014()))
        }
    }

    fun getMonster(name: String): Monster {
        for (monster in monsters) {
            if (monster.name == name) {
                return monster
            }
        }
        throw IllegalArgumentException("monster not found: "+name)
    }

    fun getMonsterOrNull(name: String): Monster? {
        try {
            return if (name.isEmpty()) null else getMonster(name)
        }
        catch (e: Exception) {
            println("unable to find monster with name = $name, $e")
            e.printStackTrace()
            return null
        }
    }


    fun addWStoCamelCase(input: String): String {
        return input.replace(Regex("(?<!^)([A-Z])"), " $1")
    }

    // logging ...
    fun debug(str: String) {
        LoggerFactory.get(Globals::class.simpleName ?: "no simpleName").debug{str}
    }

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
    }
}