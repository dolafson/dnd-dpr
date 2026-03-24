package com.vikinghelmet.dnd.dprapp.data

import androidx.compose.ui.graphics.Color
import com.vikinghelmet.dnd.dpr.character.feats.Feat
import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import com.vikinghelmet.dnd.dpr.editable.EditableCharacter
import com.vikinghelmet.dnd.dpr.editable.PlanLevel
import com.vikinghelmet.dnd.dpr.spells.Spell
import com.vikinghelmet.dnd.dpr.util.Globals

class PlanViewSpell(val spellLevel: Int, var selectedSpell: String, val options: List<Pair<String,Color>>){
    override fun toString(): String {
        return "($spellLevel: $selectedSpell)"
    }
}

class PlanViewLevel(
    val level: Int,
    val planLevel: PlanLevel,

    // these fields are transient, useful when loading/displaying screen
    val addFeat: Boolean,
    val addFS: Boolean, // subset of feat
    val addSubclass: Boolean,

    // spells are a superset of what's in the planLevel; this version includes the list of options available
    var spellsToAdd: List<PlanViewSpell> = mutableListOf()
) {
    var subclass: String? = planLevel.subclass
    var feat: Feat? = planLevel.feat
    var asi1: AbilityType? = planLevel.asi1
    var asi2: AbilityType? = planLevel.asi2

    constructor(level: Int, c: EditableCharacter) : this(
        level,
        c.editableFields.plan["$level"]!!,
        c.getLevelsForAbilityIncrease().contains(level),
        c.getLevelsForFightingStyle().contains(level),
        c.getSubclassLevel() == level
    ) {
        // the rest of this constructor is all about building spellsToAdd,
        // a combination of available spell slots, interleaved with planned spells
        val spellsToAdd = mutableListOf<PlanViewSpell>()

        val listOfSpellsFromPlanLevel = planLevel.spells.mapNotNull { spellName ->
            try { Globals.getSpellOrNull(spellName, c.is2014()) } catch (e: Exception) { null }
        }

        val mapOfSpellLevelToSpellsChosen = mutableMapOf<Int, Iterator<Spell>>()
        for (spellLevel in 1..9) {
            mapOfSpellLevelToSpellsChosen.put (spellLevel,
                listOfSpellsFromPlanLevel.filter { spellLevel == it.properties.Level }.iterator())
        }

        val newSlots = c.getSpellSlotsGainedAtCharacterLevel(level)

        for (id in newSlots.indices) repeat (newSlots[id]) {
            val spellLevel = id + 1

            val iter = mapOfSpellLevelToSpellsChosen[spellLevel]!!
            val spell = if (iter.hasNext()) iter.next() else null

            val options = c.getSpellsForClass().filter { it.properties.Level == spellLevel }.map { it.name }

            spellsToAdd.add(
                PlanViewSpell(spellLevel, spell?.name ?: "", options.map { it -> Pair(it, Color.Black) }.toList())
            )
        }

        // check for any unassigned spells ... ugh
        // somehow the character is able to prepare more spells than the slot table allows
        for (spellLevel in 1..9) {
            val options = c.getSpellsForClass().filter { it.properties.Level == spellLevel }.map { it.name }

            val iterator = mapOfSpellLevelToSpellsChosen[spellLevel]!!
            while (iterator.hasNext()) {
                val spell = iterator.next()
                println ("planView: assigning 'remainder' spell to level=${ c.getLevel() }, remainder=$spell")
                spellsToAdd.add(
                    PlanViewSpell(spellLevel, spell.name, options.map { it -> Pair(it, Color.Black) }.toList())
                )
            }
        }

        //println ("level=${ level }, spellsToAdd=$spellsToAdd, listOfSpellsFromPlanLevel=$listOfSpellsFromPlanLevel")
        this.spellsToAdd = spellsToAdd
    }

    override fun toString(): String {
        return "level=$level, flags=($addFeat,$addFS), feat=($feat,$asi1,$asi2) spells=$spellsToAdd"
    }
}

data class PlanViewModel(var plan: MutableList<PlanViewLevel> = mutableListOf())
{
    var character: EditableCharacter? = null // save for later use

    override fun toString(): String {
        val buf = StringBuilder()
        plan.forEach { buf.append(it.toString()+"\n") }
        return "[$buf]"
    }

    constructor(character: EditableCharacter) : this() {
        this.character = character
        for (tmpLevel in 1..20) {
            // println("planViewConstructor, adding level = $tmpLevel")
            plan.add(PlanViewLevel (tmpLevel, character))
        }
    }

    fun toPersistentFormat(): MutableMap<String,PlanLevel> {
        val result = mutableMapOf<String,PlanLevel>()
        for (p in plan) {
            val spellList: List<String> = p.spellsToAdd.map { it.selectedSpell }.toList()
            result.put( "${p.level}", PlanLevel(p.feat, p.asi1, p.asi2, p.subclass, emptyMap(), spellList))
        }
        return result
    }

    fun getSubclassOptions(): List<Pair<String,Color>> {
        return character!!.getSubclassOptions().map { it -> Pair(it,Color.Black) }.toList()
    }
}