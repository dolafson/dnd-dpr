package com.vikinghelmet.dnd.dpr.util

import com.vikinghelmet.dnd.dpr.character.Character
import com.vikinghelmet.dnd.dpr.character.actions.ActionModifier
import com.vikinghelmet.dnd.dpr.character.feats.Feat
import com.vikinghelmet.dnd.dpr.character.race.RacialTrait
import com.vikinghelmet.dnd.dpr.monsters.Monster
import com.vikinghelmet.dnd.dpr.spells.Spell
import com.vikinghelmet.dnd.dpr.turn.Turn
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object Globals {
    var debug = false
    val spells = ArrayList<Spell>()
    val monsters = ArrayList<Monster>()

    fun debug(str: String) { if (debug) println("# "+str) }

    @Serializable
    data class FeatureSet(
        val racialTraits: List<String>,
        val actionModifiers: List<String>,
        val feats: List<String>,
    )

    fun getPercent(float: Float): String {
        return ""+((float*100).toInt()/100)
    }

    fun wrapWithQuotes(input: String): String {
        return StringBuilder().append("\"").append(input).append("\"").toString()
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
                character?.dump()
            }
        }
    }

    fun search(arg: String) {
        val searchValue = arg.split(":")[1]
        for (item in spells) if (item.name.contains(searchValue))  println(Json.encodeToString(item))
        for (item in monsters) if (item.name.contains(searchValue))  println(Json.encodeToString(item))
    }


    fun getSpell(name: String, is2014: Boolean): Spell { //  character!!.is2014()
        for (spell in spells) {
            if (spell.name == name && (spell.isSameIn2014And2024() || (is2014 == spell.is2014()))) return spell
        }
        throw IllegalArgumentException("spell not found: "+name)
    }

    fun getMonster(name: String): Monster {
        for (monster in monsters) {
            if (monster.name == name) {
                return monster
            }
        }
        throw IllegalArgumentException("monster not found: "+name)
    }
}